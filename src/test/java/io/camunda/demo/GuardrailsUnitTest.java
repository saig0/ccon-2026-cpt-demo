package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.CamundaAssert.assertThatUserTask;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.demo.services.KnowledgeBaseService;
import io.camunda.demo.services.ProductCatalogService;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.assertions.UserTaskSelectors;
import java.time.Duration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@CamundaSpringProcessTest
public class GuardrailsUnitTest {

  private static final String USER_NAME = "Luke";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;
  @MockitoBean private ProductCatalogService productCatalogService;
  @MockitoBean private KnowledgeBaseService knowledgeBaseService;

  private ProcessInstanceEvent processInstance;

  @BeforeEach
  void setup() {
    processTestContext.mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE).thenComplete();

    processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("customerName", USER_NAME),
                    entry("userRequest", "I have an issue with my robot."),
                    entry("conversationId", CustomerSupportAgentProcess.CONVERSATION_ID)))
            .startBeforeElement(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID)
            .send()
            .join();

    assertThatProcessInstance(processInstance)
        .hasActiveElements(byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID));
  }

  @Test
  void shouldEscalateToHuman() {
    // when: activate "Inform user about escalation" inside the ad-hoc subprocess,
    // which sends a message and then throws an escalation event caught by the boundary event
    final String agentReply =
        "Hi Luke, I'm sorry but I wasn't able to resolve your issue. I'm escalating you to a human agent now.";

    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result ->
            result
                .activateElement(CustomerSupportAgentProcess.INFORM_USER_ABOUT_ESCALATION_ELEMENT_ID)
                .variable("toolCall", Map.of("agentReply", agentReply)));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of("conversation_outcome", "HUMAN_ESCALATION"));

    // then: human intervention user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(
            byName("Inform user about escalation"), byName("Analyse conversation"))
        .hasLocalVariable(byName("Inform user about escalation"), "message", agentReply);

    assertThatUserTask(UserTaskSelectors.byElementId(CustomerSupportAgentProcess.HUMAN_ESCALATION_ELEMENT_ID))
        .hasName("Human escalation")
        .hasPriority(75);
  }

  @Test
  void shouldHandleAgentError() {
    // when: agent job throws a BPMN error (e.g. MAXIMUM_NUMBER_OF_MODEL_CALLS_REACHED
    // is mapped to AGENT_ERROR via the error expression)
    processTestContext.throwBpmnErrorFromJob(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID), "AGENT_ERROR");

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of("conversation_outcome", "AGENT_ERROR"));

    // then: inform user and create human intervention user task
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(byName("Analyse conversation"), byName("Report error to user"))
        .hasLocalVariableSatisfies(
            byName("Report error to user"),
            "message",
            String.class,
            message ->
                assertThat(message)
                    .contains(
                        "Human escalation",
                        "Oops, something went wrong",
                        "One of our support agents will follow up"));

    assertThatUserTask(UserTaskSelectors.byElementId(CustomerSupportAgentProcess.HUMAN_INTERVENTION_ELEMENT_ID))
        .hasName("Human intervention")
        .hasPriority(100);
  }

  @Test
  void shouldHandleTimeout() {
    // when: timer boundary fires (configured for 1 hour; advance time by 1 hour to trigger it)
    processTestContext.increaseTime(Duration.ofHours(1));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of("conversation_outcome", "OKAY"));

    // then
    assertThatProcessInstance(processInstance)
        .isCompleted()
        .hasCompletedElements(byName("Analyse conversation"));
  }

  @Test
  void shouldCreateUserTaskToReviewConversation() {
    // when: ad-hoc subprocess completes normally and agent improvements are needed
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of("conversation_outcome", "AGENT_IMPROVEMENTS"));

    // then: review conversation user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Analyse conversation"));

    assertThatUserTask(UserTaskSelectors.byElementId(CustomerSupportAgentProcess.REVIEW_CONVERSATION_ELEMENT_ID))
        .hasName("Review conversation")
        .hasPriority(25);
  }

  @Test
  void shouldInformUserAndCreateUserTaskWhenAgentLeftConversation() {
    // when: ad-hoc subprocess completes normally but analyze conversation finds agent just left
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId(CustomerSupportAgentProcess.ANALYZE_CONVERSATION_ELEMENT_ID),
        Map.of("conversation_outcome", "AGENT_ERROR"));

    // then: inform user and create human intervention user task
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElementsInOrder(byName("Analyse conversation"), byName("Report error to user"))
        .hasLocalVariableSatisfies(
            byName("Report error to user"),
            "message",
            String.class,
            message ->
                assertThat(message)
                    .contains(
                        "Human escalation",
                        "Oops, something went wrong",
                        "One of our support agents will follow up"));

    assertThatUserTask(UserTaskSelectors.byElementId(CustomerSupportAgentProcess.HUMAN_INTERVENTION_ELEMENT_ID))
        .hasName("Human intervention")
        .hasPriority(100);
  }
}
