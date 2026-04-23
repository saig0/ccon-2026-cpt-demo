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

@SpringBootTest
@CamundaSpringProcessTest
public class CustomerSupportAgentProcessTest {

  private static final String CONVERSATION_ID = "conversation-1";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @BeforeEach
  void setupMocks() {
    processTestContext.mockJobWorker("send-chat-message").thenComplete();
  }

  @Test
  void happyPath() {
    // given
    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("customerName", "Luke"),
                    entry("userRequest", "I have an issue with my robots"),
                    entry("conversationId", CONVERSATION_ID)))
            .send()
            .join();

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId("customer-support-agent"), result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "OKAY"));

    // then
    assertThatProcessInstance(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(byId("customer-support-agent"), byId("analyze-conversation"));
  }
}
