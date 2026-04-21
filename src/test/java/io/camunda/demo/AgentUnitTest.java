package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.client.api.search.enums.ElementInstanceType;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class AgentUnitTest {

  private static final String CONVERSATION_ID = "conversation-1";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

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
                    entry("customerName", "Luke"),
                    entry("userRequest", "I have an issue with my robots"),
                    entry("conversationId", CONVERSATION_ID)))
            .startBeforeElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
            .send()
            .join();

    assertThatProcessInstance(processInstance)
        .hasActiveElements(byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID));
  }

  @Test
  void userReply() {
    // given
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement("send-agent-reply")
                .variable("message", "Don't panic. I will help you."));

    // when
    processTestContext.completeJob(byElementId("send-agent-reply"));

    assertThatProcessInstance(processInstance).isWaitingForMessage("user-message", CONVERSATION_ID);

    final String userReply = "Thanks, that would be great!";
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
  }
}
