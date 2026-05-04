package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.CamundaAssert.assertThatUserTask;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.assertions.UserTaskSelectors;
import io.camunda.process.test.api.testCases.TestCase;
import io.camunda.process.test.api.testCases.TestCaseRunner;
import io.camunda.process.test.api.testCases.TestCaseSource;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** Test cases to verify the guardrail logic in the customer support agent process. */
@SpringBootTest
@CamundaSpringProcessTest
public class AgentGuardrailsTest {

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;
  @Autowired private TestCaseRunner testCaseRunner;

  @ParameterizedTest
  @TestCaseSource(fileNames = "agent-guardrails-test.json")
  void runJsonTestCase(final TestCase testCase, final String fileName) {
    testCaseRunner.run(testCase);
  }

  private ProcessInstanceEvent createProcessInstance() {
    processTestContext
        .mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE)
        .thenComplete();

    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                new CustomerSupportAgentProcess.ConversationRequest(
                    "Luke",
                    "I have an issue with my robot.",
                    CustomerSupportAgentProcess.CONVERSATION_ID))
            .startBeforeElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
            .send()
            .join();

    assertThatProcessInstance(processInstance)
        .hasActiveElements(byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID));

    return processInstance;
  }

  @Test
  @DisplayName("Should escalate to human when agent cannot resolve the issue")
  void shouldEscalateToHuman() {
    // given
    final ProcessInstanceEvent processInstance = createProcessInstance();

    // when: activate "Inform user about escalation" inside the ad-hoc subprocess,
    // which sends a message and then throws an escalation event caught by the boundary event
    final String agentReply =
        "Hi Luke, I'm sorry but I wasn't able to resolve your issue. I'm escalating you to a human agent now.";

    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(
                    CustomerSupportAgentProcess.INFORM_USER_ABOUT_ESCALATION_ELEMENT_ID)
                .variable(
                    CustomerSupportAgentProcess.Variables.TOOL_CALL,
                    Map.of("agentReply", agentReply)));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of(CustomerSupportAgentProcess.Variables.CONVERSATION_OUTCOME, "HUMAN_ESCALATION"));

    // then: human intervention user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasActiveElements(byId(CustomerSupportAgentProcess.HUMAN_ESCALATION_ELEMENT_ID))
        // Verify the agent reply message
        .hasLocalVariable(
            byId(CustomerSupportAgentProcess.INFORM_USER_ABOUT_ESCALATION_ELEMENT_ID),
            CustomerSupportAgentProcess.Variables.SEND_CHAT_MESSAGE,
            agentReply)
        // Verify the agent variable as an input for the "Analyze conversation" task
        .hasVariable(
            CustomerSupportAgentProcess.Variables.CUSTOMER_SUPPORT_AGENT,
            Map.of(
                "escalation",
                "The agent left the conversation to escalate to a human for taking over."));

    assertThatUserTask(
            UserTaskSelectors.byElementId(CustomerSupportAgentProcess.HUMAN_ESCALATION_ELEMENT_ID))
        .hasName("Human escalation")
        .hasPriority(75);
  }

  @Test
  @DisplayName("Should handle agent errors and inform the user about it")
  void shouldHandleAgentError() {
    // given
    final ProcessInstanceEvent processInstance = createProcessInstance();

    // when: agent job throws a BPMN error (e.g. MAXIMUM_NUMBER_OF_MODEL_CALLS_REACHED
    // is mapped to AGENT_ERROR via the error expression)
    final String errorCode = "MAXIMUM_NUMBER_OF_MODEL_CALLS_REACHED";
    final String errorMessage =
        "The agent has reached the maximum number of model calls allowed for this conversation.";

    processTestContext.throwBpmnErrorFromJob(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        "AGENT_ERROR",
        Map.ofEntries(Map.entry("code", errorCode), Map.entry("message", errorMessage)));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of(CustomerSupportAgentProcess.Variables.CONVERSATION_OUTCOME, "AGENT_ERROR"));

    // then: inform user and create human intervention user task
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.REPORT_ERROR_TO_USER_ELEMENT_ID))
        .hasActiveElements(byId(CustomerSupportAgentProcess.HUMAN_INTERVENTION_ELEMENT_ID))
        // Verify the agent reply message
        .hasLocalVariableSatisfies(
            byId(CustomerSupportAgentProcess.REPORT_ERROR_TO_USER_ELEMENT_ID),
            CustomerSupportAgentProcess.Variables.SEND_CHAT_MESSAGE,
            String.class,
            message ->
                assertThat(message)
                    .contains(
                        "Human escalation",
                        "Oops, something went wrong",
                        "One of our support agents will follow up"))
        // Verify the agent variable as an input for the "Analyze conversation" task
        .hasVariable(
            CustomerSupportAgentProcess.Variables.CUSTOMER_SUPPORT_AGENT,
            Map.of(
                "error",
                Map.ofEntries(Map.entry("code", errorCode), Map.entry("message", errorMessage))));

    assertThatUserTask(
            UserTaskSelectors.byElementId(
                CustomerSupportAgentProcess.HUMAN_INTERVENTION_ELEMENT_ID))
        .hasName("Human intervention")
        .hasPriority(100);
  }

  @Test
  @DisplayName("Should handle conversation timeout")
  void shouldHandleTimeout() {
    // given
    final ProcessInstanceEvent processInstance = createProcessInstance();

    // when: timer boundary fires (configured for 15 minutes; advance time to trigger it)
    processTestContext.increaseTime(Duration.ofMinutes(15));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of(CustomerSupportAgentProcess.Variables.CONVERSATION_OUTCOME, "OKAY"));

    // then
    assertThatProcessInstance(processInstance)
        .isCompleted()
        .hasCompletedElements(byId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID))
        // Verify the agent variable as an input for the "Analyze conversation" task
        .hasVariable(
            CustomerSupportAgentProcess.Variables.CUSTOMER_SUPPORT_AGENT,
            Map.of("timeout", "The agent left the conversation after reaching the timeout."));
  }

  @Test
  @DisplayName("Should review conversation and provide feedback for agent improvements")
  void shouldReviewConversation() {
    // given
    final ProcessInstanceEvent processInstance = createProcessInstance();

    // when: ad-hoc subprocess completes normally and agent improvements are needed
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of(CustomerSupportAgentProcess.Variables.CONVERSATION_OUTCOME, "AGENT_IMPROVEMENTS"));

    // then: review conversation user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasActiveElements(byId(CustomerSupportAgentProcess.REVIEW_CONVERSATION_ELEMENT_ID));

    assertThatUserTask(
            UserTaskSelectors.byElementId(
                CustomerSupportAgentProcess.REVIEW_CONVERSATION_ELEMENT_ID))
        .hasName("Review conversation")
        .hasPriority(25);
  }
}
