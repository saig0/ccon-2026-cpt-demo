package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static io.camunda.process.test.api.assertions.ProcessInstanceSelectors.byProcessId;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.enums.ElementInstanceType;
import io.camunda.demo.dto.*;
import io.camunda.demo.model.OrderStatus;
import io.camunda.demo.model.RobotIntent;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.demo.services.KnowledgeBaseService;
import io.camunda.demo.services.ProductCatalogService;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.mock.JobWorkerMockBuilder.JobWorkerMock;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Test cases to verify the correct execution of the tool calls in the customer support agent
 * process.
 */
@SpringBootTest
@CamundaSpringProcessTest
public class AgentToolCallsTest {

  private static final String USER_NAME = "Luke";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;
  @MockitoBean private ProductCatalogService productCatalogService;
  @MockitoBean private KnowledgeBaseService knowledgeBaseService;

  private ProcessInstanceEvent processInstance;

  @BeforeEach
  void createProcessInstance() {
    // Create the process instance at the ad-hoc sub-process
    processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                new CustomerSupportAgentProcess.ConversationRequest(
                    USER_NAME,
                    "I have an issue with my robot.",
                    CustomerSupportAgentProcess.CONVERSATION_ID))
            .startBeforeElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
            .terminateAfterElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
            .send()
            .join();

    assertThatProcessInstance(processInstance)
        .hasActiveElements(byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID));
  }

  @Test
  @DisplayName("Should reply to user with agent message and receive user reply")
  void shouldReplyToUser() {
    // given
    final String agentReply =
        "Hi Luke, I see that you have two robots: R2-D2 and C3P0. Which one are you having issues with?";
    final String userReply = "It's C3P0. He doesn't stop talking.";

    final JobWorkerMock sendChatMessageMockWorker =
        processTestContext
            .mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE)
            .thenComplete();

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID)
                .variable(
                    CustomerSupportAgentProcess.Variables.TOOL_CALL,
                    Map.of("agentReply", agentReply)));

    assertThatProcessInstance(processInstance)
        .isWaitingForMessage(
            CustomerSupportAgentProcess.USER_MESSAGE_RECEIVED_NAME,
            CustomerSupportAgentProcess.CONVERSATION_ID);

    CustomerSupportAgentProcess.publishUserMessage(
            client, userReply, CustomerSupportAgentProcess.CONVERSATION_ID);

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariable(CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT, userReply);

    // Verify the agent reply message
    assertThat(sendChatMessageMockWorker.getActivatedJobs())
        .hasSize(1)
        .first()
        .extracting(job -> job.getVariable(CustomerSupportAgentProcess.Variables.SEND_CHAT_MESSAGE))
        .isEqualTo(agentReply);
  }

  @Test
  @DisplayName("Should load user data from the customer database service")
  void shouldLoadUser() {
    // given
    final CustomerDto customer =
        new CustomerDto(
            1L,
            USER_NAME,
            "luke.skywalker@tatooine.galaxy",
            new AddressDto("Moisture Farm, Jundland Wastes", "Tatooine", "Outer Rim Territories"),
            new PaymentInfoDto("GALACTIC_CREDITS", "GC-77890-SKY"),
            true,
            false,
            List.of());

    when(customerDatabaseService.findCustomerByName(USER_NAME)).thenReturn(Optional.of(customer));

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID)
                .variable(
                    CustomerSupportAgentProcess.Variables.TOOL_CALL,
                    Map.of("customerName", USER_NAME)));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariableSatisfies(
            CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT,
            CustomerDto.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(customer));

    // Verify that the user is loaded from the service
    verify(customerDatabaseService).findCustomerByName(USER_NAME);
  }

  @Test
  @DisplayName("Should load product catalog")
  void shouldLoadProductCatalog() {
    // given
    final List<RobotDto> robots =
        List.of(
            new RobotDto(
                1L,
                "C3PO",
                "1.0",
                "C-3PO Protocol Droid v1.0",
                "The original human-cyborg relations droid, fluent in over six million forms of communication. Polite, knowledgeable, and occasionally over-dramatic.",
                RobotIntent.TRANSLATION,
                BigDecimal.valueOf(9999.99),
                List.of()));

    when(productCatalogService.findAllRobots()).thenReturn(robots);

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result.activateElement(CustomerSupportAgentProcess.LOAD_PRODUCT_CATALOG_ELEMENT_ID));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariableSatisfies(
            CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT,
            RobotList.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(robots));

    // Verify that the products are loaded from the service
    verify(productCatalogService).findAllRobots();
  }

  @Test
  @DisplayName("Should load knowledge base entries")
  void shouldLoadKnowledgeBase() {
    // given
    final String keyword = "c3po";
    final List<KnowledgeBaseEntryDto> knowledgeBaseEntries =
        List.of(
            new KnowledgeBaseEntryDto(
                4L,
                "C-3PO is talking too much and cannot be silenced.",
                List.of(keyword, "verbosity", "talking", "silence"),
                "This is normal behaviour for a C-3PO unit — it is designed for human-cyborg relations. You can purchase the Reduced Verbosity Module upgrade to filter out unnecessary commentary by up to 94.7%."));

    when(knowledgeBaseService.findByKeyword(keyword)).thenReturn(knowledgeBaseEntries);

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID)
                .variable(
                    CustomerSupportAgentProcess.Variables.TOOL_CALL, Map.of("keyword", keyword)));

    // then
    assertThatProcessInstance(processInstance)
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariableSatisfies(
            CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT,
            KnowledgeBaseEntryList.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(knowledgeBaseEntries));

    // Verify that the knowledge base is queried
    verify(knowledgeBaseService).findByKeyword(keyword);
  }

  @Test
  @DisplayName("Should calculate discount based on the decision table")
  void shouldCalculateDiscount() {
    // given
    // Mock the decision to return a 15% discount. We verify the decision logic separately.
    final int discount = 15;
    processTestContext.mockDmnDecision(CustomerSupportAgentProcess.DISCOUNT_DECISION_ID, discount);

    final CustomerSupportAgentProcess.DiscountDecisionInput discountDecisionInput =
        new CustomerSupportAgentProcess.DiscountDecisionInput(2, 1, 0);

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.CALCULATE_DISCOUNT_ELEMENT_ID)
                .variable(CustomerSupportAgentProcess.Variables.TOOL_CALL, discountDecisionInput));

    // then
    assertThatProcessInstance(processInstance)
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariable(CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT, discount)
        // Verify the input mapping as the contract of the decision
        .hasLocalVariable(
            byId(CustomerSupportAgentProcess.CALCULATE_DISCOUNT_ELEMENT_ID),
            "numberOfPreviouslyPurchasedRobots",
            discountDecisionInput.numberOfPreviouslyPurchasedRobots())
        .hasLocalVariable(
            byId(CustomerSupportAgentProcess.CALCULATE_DISCOUNT_ELEMENT_ID),
            "numberOfRobotsInOrder",
            discountDecisionInput.numberOfRobotsInOrder())
        .hasLocalVariable(
            byId(CustomerSupportAgentProcess.CALCULATE_DISCOUNT_ELEMENT_ID),
            "numberOfUpgradesInOrder",
            discountDecisionInput.numberOfUpgradesInOrder());
  }

  @Test
  @DisplayName("Should call the order process as a child process")
  void shouldOrderItems() {
    // given
    final AddressDto shipmentAddress =
        new AddressDto("1 Moisture Farm Rd", "Anchorhead", "Tatooine");
    final BigDecimal paymentAmount = BigDecimal.valueOf(9999.99);

    final OrderRequestDto orderRequest =
        new OrderRequestDto(
            1L, shipmentAddress, paymentAmount, List.of(new OrderItemInputDto(1L, null, 1)));

    final OrderDto order =
        new OrderDto(
            42L,
            LocalDate.now(),
            shipmentAddress,
            LocalDate.now().plusDays(7),
            LocalDate.now(),
            paymentAmount,
            List.of(),
            OrderStatus.PREPARED_FOR_SHIPPING);

    // Mock the child process to reduce the test scope. We verify the child process logic
    // separately.
    processTestContext.mockChildProcess(
        CustomerSupportAgentProcess.ORDER_PROCESS_ID, Map.of("order", order));

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.ORDER_ITEMS_ELEMENT_ID)
                .variable(
                    CustomerSupportAgentProcess.Variables.TOOL_CALL,
                    Map.of("orderRequest", orderRequest)));

    // then
    assertThatProcessInstance(processInstance)
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify the tool call result
        .hasVariableSatisfies(
            CustomerSupportAgentProcess.Variables.TOOL_CALL_RESULT,
            OrderDto.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(order));

    // Verify the input mappings as the contract of the child process
    assertThatProcessInstance(byProcessId(CustomerSupportAgentProcess.ORDER_PROCESS_ID))
        .hasVariableSatisfies(
            "orderRequest",
            OrderRequestDto.class,
            value -> assertThat(value).isEqualTo(orderRequest));
  }

  private static class RobotList extends ArrayList<RobotDto> {}

  private static class KnowledgeBaseEntryList extends ArrayList<KnowledgeBaseEntryDto> {}
}
