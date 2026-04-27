package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;

public class CustomerSupportAgentProcess {

  // BPMN constants

  public static final String PROCESS_ID = "customer-support-agent-process";
  public static final String AD_HOC_SUB_PROCESS_ELEMENT_ID = "customer-support-agent";

  public static final String USER_MESSAGE_RECEIVED_NAME = "user-message";

  public static final String CONVERSATION_ID = "conversation-1";

  // BPMN data objects

  public record ConversationRequest(String userName, String message, String conversationId) {}

  public record UserMessage(String message) {}

  // Process helpers

  public static ProcessInstanceEvent createProcessInstance(
      final CamundaClient camundaClient,
      final String userName,
      final String message,
      final String conversationId) {

    return camundaClient
        .newCreateInstanceCommand()
        .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
        .latestVersion()
        .variables(new ConversationRequest(userName, message, conversationId))
        .send()
        .join();
  }

  public static void publishUserMessage(
      final CamundaClient camundaClient, final String message, final String conversationId) {
    camundaClient
        .newPublishMessageCommand()
        .messageName(USER_MESSAGE_RECEIVED_NAME)
        .correlationKey(conversationId)
        .variables(new UserMessage(message))
        .send()
        .join();
  }

  public static void awaitUserMessage(
      final ProcessInstanceEvent processInstance, final String conversationId) {
    assertThatProcessInstance(processInstance)
        .isWaitingForMessage(USER_MESSAGE_RECEIVED_NAME, conversationId);
  }
}
