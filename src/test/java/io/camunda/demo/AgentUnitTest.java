package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.enums.ElementInstanceType;
import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.CustomerDto;
import io.camunda.demo.dto.PaymentInfoDto;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.mock.JobWorkerMockBuilder.JobWorkerMock;
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
public class AgentUnitTest {

  private static final String USER_NAME = "Luke";
  private static final String CONVERSATION_ID = "conversation-1";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;

  private ProcessInstanceEvent processInstance;

  @BeforeEach
  void createProcessInstance() {
    processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("customerName", USER_NAME),
                    entry("userRequest", "I have an issue with my robot."),
                    entry("conversationId", CONVERSATION_ID)))
            .startBeforeElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
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
        processTestContext.mockJobWorker("send-chat-message").thenComplete();

    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("send-agent-reply")
                .variable("toolCall", Map.of("agentReply", agentReply)));

    // when
    assertThatProcessInstance(processInstance).isWaitingForMessage("user-message", CONVERSATION_ID);

    client
        .newPublishMessageCommand()
        .messageName("user-message")
        .correlationKey(CONVERSATION_ID)
        .variables(Map.of("message", userReply))
        .send()
        .join();

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(byName("Send agent reply"), byName("User message received"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariable("toolCallResult", userReply);

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
                .activateElement("load-customer-data")
                .variable("toolCall", Map.of("customerName", USER_NAME)));

    // then
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Load customer data"))
        .hasCompletedElement(
            byElementType(ElementInstanceType.AD_HOC_SUB_PROCESS_INNER_INSTANCE), 1)
        .hasVariableSatisfies(
            "toolCallResult",
            CustomerDto.class,
            toolCallResult -> assertThat(toolCallResult).isEqualTo(customer));
  }
}
