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
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the customer support agent process using a real LLM (AWS Bedrock) and the
 * real data services backed by the H2 database pre-loaded with {@code data.sql}.
 *
 * <p>The LLM agent drives the conversation naturally via the {@code connector-agentic-ai}
 * connector. Customer, product and knowledge-base data are read from the real in-memory database,
 * seeded by {@code src/main/resources/data.sql}.
 *
 * <p>Scenario: User Hiro reports that his robot is losing air. The agent loads his customer data
 * (Baymax), searches the knowledge base for a solution, and replies with the fix.
 *
 * <p>Requires the environment variables {@code AWS_BEDROCK_ACCESS_KEY} and
 * {@code AWS_BEDROCK_SECRET_KEY} to be set.
 */
@SpringBootTest(
    properties = {
      // Enable the AI Agent job-worker connector so the real LLM handles the conversation loop
      "camunda.connector.agenticai.aiagent.job-worker.enabled=true"
    })
@CamundaSpringProcessTest
@EnabledIfEnvironmentVariable(named = "AWS_BEDROCK_ACCESS_KEY", matches = ".+")
@EnabledIfEnvironmentVariable(named = "AWS_BEDROCK_SECRET_KEY", matches = ".+")
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

    // when - the LLM agent (via connector-agentic-ai) runs the conversation loop using real
    //   customer data (Hiro + Baymax from data.sql) and real knowledge base entries:
    //   1. loads Hiro's customer data including his Baymax v1.0 order
    //   2. searches the knowledge base for a solution (Baymax air-loss entry)
    //   3. calls send-agent-reply with the fix and waits for user input
    assertThatProcessInstance(processInstance)
        .isWaitingForMessage("user-message", CONVERSATION_ID);

    // when - the user confirms the issue is resolved
    client
        .newPublishMessageCommand()
        .messageName("user-message")
        .correlationKey(CONVERSATION_ID)
        .variables(Map.of("message", "Thank you, that fixed it! I don't need any more help."))
        .send()
        .join();

    // when - the LLM ends the conversation; analyze-conversation is completed manually
    //   (the outbound connector is disabled in test configuration to allow direct completion)
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
