package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.*;
import static io.camunda.process.test.api.assertions.JobSelectors.byElementId;
import static java.util.Map.entry;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.services.CustomerDatabaseService;
import io.camunda.demo.services.KnowledgeBaseService;
import io.camunda.demo.services.ProductCatalogService;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
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
  private static final String CONVERSATION_ID = "conversation-1";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @MockitoBean private CustomerDatabaseService customerDatabaseService;
  @MockitoBean private ProductCatalogService productCatalogService;
  @MockitoBean private KnowledgeBaseService knowledgeBaseService;

  private ProcessInstanceEvent processInstance;

  @BeforeEach
  void setup() {
    processTestContext.mockJobWorker("send-chat-message").thenComplete();

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
  void shouldEscalateToHuman() {
    // when: activate "Inform user about escalation" inside the ad-hoc subprocess,
    // which sends a message and then throws an escalation event caught by the boundary event
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.activateElement("Activity_1m3rzv6"));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "HUMAN_ESCALATION"));

    // then: human intervention user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(
            byName("Inform user about escalation"), byName("Analyse conversation"))
        .hasActiveElements(byId("Activity_0qjlvsg"));
  }

  @Test
  void shouldHandleAgentError() {
    // when: agent job throws a BPMN error (e.g. MAXIMUM_NUMBER_OF_MODEL_CALLS_REACHED
    // is mapped to AGENT_ERROR via the error expression)
    processTestContext.throwBpmnErrorFromJob(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID), "AGENT_ERROR");

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "AGENT_ERROR"));

    // then: inform user and create human intervention user task
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Inform user"), byName("Analyse conversation"))
        .hasActiveElements(byId("Activity_0xyxkvi"));
  }

  @Test
  void shouldHandleTimeout() {
    // when: timer boundary fires (configured for 1 hour; advance time by 2 hours to trigger it)
    processTestContext.increaseTime(Duration.ofHours(2));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "HUMAN_ESCALATION"));

    // then: human intervention user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Analyse conversation"))
        .hasActiveElements(byId("Activity_0qjlvsg"));
  }

  @Test
  void shouldCreateUserTaskToReviewConversation() {
    // when: ad-hoc subprocess completes normally and agent improvements are needed
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "AGENT_IMPROVEMENTS"));

    // then: review conversation user task is created
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Analyse conversation"))
        .hasActiveElements(byId("Activity_0jl8d0j"));
  }

  @Test
  void shouldInformUserAndCreateUserTaskWhenAgentLeftConversation() {
    // when: ad-hoc subprocess completes normally but analyze conversation finds agent just left
    processTestContext.completeJobOfAdHocSubProcess(
        byElementId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
        result -> result.completionConditionFulfilled(true));

    processTestContext.completeJob(
        byElementId("analyze-conversation"), Map.of("conversation_outcome", "AGENT_ERROR"));

    // then: inform user and create human intervention user task
    assertThatProcessInstance(processInstance)
        .isActive()
        .hasCompletedElements(byName("Inform user"), byName("Analyse conversation"))
        .hasActiveElements(byId("Activity_0xyxkvi"));
  }
}
