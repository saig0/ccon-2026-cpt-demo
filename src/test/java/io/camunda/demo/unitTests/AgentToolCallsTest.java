package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
public class AgentToolCallsTest {

  private static final String USER_NAME = "Luke";

  private static final String TOOL_CALL_RESULT_VARIABLE = "toolCallResult";
  private static final String TOOL_CALL_VARIABLE = "toolCall";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;
  @MockitoBean private ProductCatalogService productCatalogService;
  @MockitoBean private KnowledgeBaseService knowledgeBaseService;

  private ProcessInstanceEvent processInstance;

  @BeforeEach
  void createProcessInstance() {
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
                .variable(TOOL_CALL_VARIABLE, Map.of("agentReply", agentReply)));

    assertThatProcessInstance(processInstance)
        .isWaitingForMessage(
            CustomerSupportAgentProcess.USER_MESSAGE_RECEIVED_NAME,
            CustomerSupportAgentProcess.CONVERSATION_ID);

    CustomerSupportAgentProcess.publishUserMessage(
            client, userReply, CustomerSupportAgentProcess.CONVERSATION_ID);

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(byName("Send agent reply"), byName("User message received"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariable(TOOL_CALL_RESULT_VARIABLE, userReply);

    assertThat(sendChatMessageMockWorker.getActivatedJobs())
        .hasSize(1)
        .first()
        .extracting(job -> job.getVariable("message"))
        .isEqualTo(agentReply);
  }

  @Test
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
                .variable(TOOL_CALL_VARIABLE, Map.of("customerName", USER_NAME)));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Load customer data"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariableSatisfies(
            TOOL_CALL_RESULT_VARIABLE,
            CustomerDto.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(customer));
  }

  @Test
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
        .hasCompletedElements(byName("Load product catalog"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariableSatisfies(
            TOOL_CALL_RESULT_VARIABLE,
            RobotList.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(robots));
  }

  @Test
  void shouldLoadKnowledgeBase() {
    // given
    final List<KnowledgeBaseEntryDto> knowledgeBaseEntries =
        List.of(
            new KnowledgeBaseEntryDto(
                4L,
                "C-3PO is talking too much and cannot be silenced.",
                List.of("c3po", "verbosity", "talking", "silence"),
                "This is normal behaviour for a C-3PO unit — it is designed for human-cyborg relations. You can purchase the Reduced Verbosity Module upgrade to filter out unnecessary commentary by up to 94.7%."));

    when(knowledgeBaseService.findByKeyword("c3po")).thenReturn(knowledgeBaseEntries);

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID)
                .variable(TOOL_CALL_VARIABLE, Map.of("keyword", "c3po")));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Search knowledge base"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariableSatisfies(
            TOOL_CALL_RESULT_VARIABLE,
            KnowledgeBaseEntryList.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(knowledgeBaseEntries));
  }

  @Test
  void shouldCalculateDiscount() {
    // given
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
                .variable(TOOL_CALL_VARIABLE, discountDecisionInput));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Calculate discount"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        // Verify decision output mapping
        .hasVariable(TOOL_CALL_RESULT_VARIABLE, discount)
        // Verify decision input mapping
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
  void shouldOrderItems() {
    // given
    final AddressDto shipmentAddress =
        new AddressDto("1 Moisture Farm Rd", "Anchorhead", "Tatooine");
    final BigDecimal paymentAmount = BigDecimal.valueOf(9999.99);
    final List<OrderItemInput> orderItems = List.of(new OrderItemInput(1L, null, 1));

    final OrderDto finalOrder =
        new OrderDto(
            42L,
            LocalDate.now(),
            shipmentAddress,
            LocalDate.now().plusDays(7),
            LocalDate.now(),
            paymentAmount,
            List.of(),
            OrderStatus.PREPARED_FOR_SHIPPING);

    processTestContext.mockChildProcess("order-process", Map.of("order", finalOrder));

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.ORDER_ITEMS_ELEMENT_ID)
                .variable(
                    TOOL_CALL_VARIABLE,
                    Map.of(
                        "customerId", 1L,
                        "shipmentAddress", shipmentAddress,
                        "paymentAmount", paymentAmount,
                        "orderItems", orderItems)));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Order items"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariableSatisfies(
            TOOL_CALL_RESULT_VARIABLE,
            OrderDto.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(finalOrder));
  }

  private static class RobotList extends ArrayList<RobotDto> {}

  private static class KnowledgeBaseEntryList extends ArrayList<KnowledgeBaseEntryDto> {}
}
