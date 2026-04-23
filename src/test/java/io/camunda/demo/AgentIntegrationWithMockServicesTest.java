package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.CustomerDto;
import io.camunda.demo.dto.KnowledgeBaseEntryDto;
import io.camunda.demo.dto.PaymentInfoDto;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.demo.services.KnowledgeBaseService;
import io.camunda.demo.services.ProductCatalogService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration test for the customer support agent process using an LLM (simulated) and mock data
 * services. The LLM agent decisions are simulated via {@code completeJobOfAdHocSubProcess}, while
 * the underlying data services are replaced with Mockito mocks for full control.
 *
 * <p>Scenario: User Hiro reports that his robot is losing air. The agent loads his customer data,
 * searches the knowledge base for a solution, and replies with the fix for Baymax's air loss.
 */
@SpringBootTest
@CamundaSpringProcessTest
public class AgentIntegrationWithMockServicesTest {

  private static final String USER_NAME = "Hiro";
  private static final String CONVERSATION_ID = "conversation-hiro-mock-1";
  private static final String USER_REQUEST = "My robot is losing air";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;
  @MockitoBean private ProductCatalogService productCatalogService;
  @MockitoBean private KnowledgeBaseService knowledgeBaseService;

  @BeforeEach
  void setupMocks() {
    processTestContext.mockJobWorker("send-chat-message").thenComplete();

    final CustomerDto hiro =
        new CustomerDto(
            4L,
            USER_NAME,
            "hiro.hamada@sfit.edu",
            new AddressDto("1234 Lucky Cat Cafe, Akihabara District", "San Francisco", "USA"),
            new PaymentInfoDto("PAYPAL", "hiro.hamada@sfit.edu"),
            true,
            false,
            List.of());
    when(customerDatabaseService.findCustomerByName(USER_NAME)).thenReturn(Optional.of(hiro));

    final List<KnowledgeBaseEntryDto> kbEntries =
        List.of(
            new KnowledgeBaseEntryDto(
                2L,
                "Baymax is losing air and deflating.",
                List.of("baymax", "air", "hole"),
                "Fix the hole in Baymax's inflatable chassis with tape. Locate the puncture by "
                    + "listening for hissing, seal it with industrial-strength duct tape, and "
                    + "schedule a full reinflation."));
    when(knowledgeBaseService.findByKeyword("air")).thenReturn(kbEntries);
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

    // when - Step 1: LLM agent loads customer data for Hiro
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("load-customer-data")
                .variable("toolCall", Map.of("customerName", USER_NAME)));

    // when - Step 2: LLM agent searches the knowledge base with keyword "air"
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
