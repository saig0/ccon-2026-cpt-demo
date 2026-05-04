package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.demo.util.CustomerSupportAgentProcessUtil;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class AgentProcessTest {

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  private CustomerSupportAgentProcessUtil processUtil;

  @BeforeEach
  void setupMocks() {
    processTestContext
        .mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE)
        .thenComplete();

    processUtil =
        new CustomerSupportAgentProcessUtil(client, CustomerSupportAgentProcess.CONVERSATION_ID);
  }

  @Test
  void shouldCompleteProcess() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance("Luke", "I have an issue with my robots");

    // when
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of(CustomerSupportAgentProcess.Variables.CONVERSATION_OUTCOME, "OKAY"));

    // then
    assertThatProcessInstance(processInstance)
        .isCompleted()
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID));
  }
}
