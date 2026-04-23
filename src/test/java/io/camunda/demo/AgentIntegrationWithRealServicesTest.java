package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the customer support agent process using an LLM (simulated) and the real
 * data services backed by the H2 database pre-loaded with {@code data.sql}.
 *
 * <p>Scenario: User Hiro reports that his robot is losing air. The agent loads his customer data
 * from the real database, searches the knowledge base for a solution, and replies with the fix for
 * Baymax's air loss.
 */
@SpringBootTest
@CamundaSpringProcessTest
public class AgentIntegrationWithRealServicesTest {

  private static final String USER_NAME = "Hiro";
  private static final String CONVERSATION_ID = "conversation-hiro-real-1";
  private static final String USER_REQUEST = "My robot is losing air";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @BeforeEach
  void setupMocks() {
    processTestContext.mockJobWorker("send-chat-message").thenComplete();
  }

  @Test
  void shouldResolveRobotAirProblem() {
    // given
    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("customerName", USER_NAME),
                    entry("userRequest", USER_REQUEST),
                    entry("conversationId", CONVERSATION_ID)))
            .send()
            .join();

    assertThatProcessInstance(processInstance)
        .hasActiveElements(byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID));

    // when - Step 1: LLM agent loads customer data for Hiro (from data.sql)
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("load-customer-data")
                .variable("toolCall", Map.of("customerName", USER_NAME)));

    // when - Step 2: LLM agent searches the knowledge base with keyword "air" (from data.sql)
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("seach-knowledge-base")
                .variable("toolCall", Map.of("keyword", "air")));

    // when - Step 3: LLM agent sends a reply to the user with the solution
    final String agentReply =
        "I found the issue! Baymax is losing air due to a hole in his inflatable chassis. "
            + "Fix the hole with duct tape and schedule a full reinflation.";
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("send-agent-reply")
                .variable("toolCall", Map.of("agentReply", agentReply)));

    assertThatProcessInstance(processInstance)
        .isWaitingForMessage("user-message", CONVERSATION_ID);

    client
        .newPublishMessageCommand()
        .messageName("user-message")
        .correlationKey(CONVERSATION_ID)
        .variables(Map.of("message", "Thank you, that fixed it!"))
        .send()
        .join();

    // when - Step 4: LLM agent ends the conversation
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "OKAY"));

    // then
    assertThatProcessInstance(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
            byId("analyze-conversation"));
  }
}
