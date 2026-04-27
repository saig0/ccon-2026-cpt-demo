package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
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
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.demo.util.CustomerSupportAgentProcessUtil;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Integration test for the customer support agent process using a real LLM (AWS Bedrock) and mock
 * data services.
 *
 * <p>Scenario: User Hiro reports that his robot is losing air. The agent loads his customer data
 * (Baymax), searches the knowledge base for a solution, and replies with the fix.
 *
 * <p>Requires the environment variables {@code AWS_BEDROCK_ACCESS_KEY} and {@code
 * AWS_BEDROCK_SECRET_KEY} to be set.
 */
@SpringBootTest(
    properties = {
      // Enable the AI connector
      "camunda.process-test.runtime-mode=managed",
      "camunda.process-test.connectors-enabled=true",
      // Set connector secrets for the AI connector
      "camunda.process-test.connectors-secrets.AWS_BEDROCK_ACCESS_KEY=${AWS_BEDROCK_ACCESS_KEY}",
      "camunda.process-test.connectors-secrets.AWS_BEDROCK_SECRET_KEY=${AWS_BEDROCK_SECRET_KEY}",
      // Configure the judge for assertions
      "camunda.process-test.judge.chat-model.provider=amazon-bedrock",
      "camunda.process-test.judge.chat-model.model=eu.anthropic.claude-haiku-4-5-20251001-v1:0",
      "camunda.process-test.judge.chat-model.region=eu-central-1",
      "camunda.process-test.judge.chat-model.credentials.access-key=${AWS_BEDROCK_ACCESS_KEY}",
      "camunda.process-test.judge.chat-model.credentials.secret-key=${AWS_BEDROCK_SECRET_KEY}"
    })
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

  private CustomerSupportAgentProcessUtil processUtil;

  @BeforeEach
  void setupMocks() {
    processTestContext.mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE).thenComplete();

    processUtil = new CustomerSupportAgentProcessUtil(client, CONVERSATION_ID);

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
    when(productCatalogService.findAllRobots()).thenReturn(List.of(baymax));

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

    processTestContext
        .when(
            () ->
                assertThatProcessInstance(
                        ProcessInstanceSelectors.byProcessId(
                            CustomerSupportAgentProcess.PROCESS_ID))
                    .isWaitingForMessage(
                        CustomerSupportAgentProcess.USER_MESSAGE_RECEIVED_NAME, CONVERSATION_ID))
        .as("Mock user reply")
        .then(
            () ->
                client
                    .newPublishMessageCommand()
                    .messageName(CustomerSupportAgentProcess.USER_MESSAGE_RECEIVED_NAME)
                    .correlationKey(CONVERSATION_ID)
                    .variables(
                        Map.of("message", "Thank you, that fixed it! I don't need any more help."))
                    .send()
                    .join());
  }

  @Test
  void shouldResolveRobotAirProblem() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance(USER_NAME, USER_REQUEST);

    // when - the LLM agent (via connector-agentic-ai) runs the conversation loop:
    //   1. loads customer data for Hiro
    //   2. searches knowledge base for a solution
    //   3. calls send-agent-reply with the fix and waits for user input

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID));

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(
            byId(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID))
        .hasLocalVariableSatisfiesJudge(
            byId(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID),
            "message",
            """
              The reply should be friendly and professional. It should contains: \
              a greeting to 'Hiro', \
              confirm that the issue is about Baymax, \
              propose a solution using a tape.""");
  }
}
