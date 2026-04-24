package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.CustomerDto;
import io.camunda.demo.dto.KnowledgeBaseEntryDto;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.dto.OrderItemDto;
import io.camunda.demo.dto.PaymentInfoDto;
import io.camunda.demo.dto.RobotDto;
import io.camunda.demo.model.RobotIntent;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.demo.services.KnowledgeBaseService;
import io.camunda.demo.services.ProductCatalogService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration test for the customer support agent process using a real LLM (AWS Bedrock) and mock
 * data services.
 *
 * <p>The LLM agent drives the conversation naturally via the {@code connector-agentic-ai}
 * connector. Data services ({@link CustomerDatabaseService}, {@link KnowledgeBaseService},
 * {@link ProductCatalogService}) are replaced with Mockito mocks for full control of the input
 * data.
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

    final RobotDto baymax =
        new RobotDto(
            9L,
            "BAYMAX",
            "1.0",
            "Baymax Personal Healthcare Companion v1.0",
            "Inflatable, non-threatening healthcare companion capable of diagnosing over 10 000 "
                + "medical conditions. Powered by a single medical-grade action chip.",
            RobotIntent.HEALTHCARE,
            BigDecimal.valueOf(6999.99),
            List.of());

    final OrderDto order =
        new OrderDto(
            6L,
            LocalDate.of(2024, 12, 24),
            new AddressDto("1234 Lucky Cat Cafe, Akihabara District", "San Francisco", "USA"),
            LocalDate.of(2024, 12, 29),
            LocalDate.of(2024, 12, 24),
            BigDecimal.valueOf(6999.99),
            List.of(new OrderItemDto(baymax, null, 1)));

    final CustomerDto hiro =
        new CustomerDto(
            4L,
            USER_NAME,
            "hiro.hamada@sfit.edu",
            new AddressDto("1234 Lucky Cat Cafe, Akihabara District", "San Francisco", "USA"),
            new PaymentInfoDto("PAYPAL", "hiro.hamada@sfit.edu"),
            true,
            false,
            List.of(order));
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
    when(knowledgeBaseService.findByKeyword(anyString())).thenReturn(kbEntries);
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

    // when - the LLM agent (via connector-agentic-ai) runs the conversation loop:
    //   1. loads customer data for Hiro
    //   2. searches knowledge base for a solution
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
