package io.camunda.demo.util;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;

public class CustomerSupportAgentProcess {

  // BPMN constants

  public static final String PROCESS_ID = "customer-support-agent-process";
  public static final String AD_HOC_SUB_PROCESS_ELEMENT_ID = "customer-support-agent";
  public static final String ANALYZE_CONVERSATION_ELEMENT_ID = "analyze-conversation";
  public static final String SEND_AGENT_REPLY_ELEMENT_ID = "send-agent-reply";
  public static final String LOAD_CUSTOMER_DATA_ELEMENT_ID = "load-customer-data";
  public static final String LOAD_PRODUCT_CATALOG_ELEMENT_ID = "load-product-catalog";
  public static final String SEARCH_KNOWLEDGE_BASE_ELEMENT_ID = "search-knowledge-base";
  public static final String INFORM_USER_ABOUT_ESCALATION_ELEMENT_ID =
      "inform-user-about-escalation";
  public static final String HUMAN_ESCALATION_ELEMENT_ID = "human-escalation";
  public static final String HUMAN_INTERVENTION_ELEMENT_ID = "human-intervention";
  public static final String REVIEW_CONVERSATION_ELEMENT_ID = "review-conversation";

  public static final String USER_MESSAGE_RECEIVED_NAME = "user-message";
  public static final String SEND_CHAT_MESSAGE_JOB_TYPE = "send-chat-message";

  public static final String CONVERSATION_ID = "conversation-1";

  public static final String DISCOUNT_DECISION_ID = "discount";

  // BPMN data objects

  public record ConversationRequest(String userName, String message, String conversationId) {}

  public record UserMessage(String message) {}

  public record DiscountDecisionInput(
      Integer numberOfPreviouslyPurchasedRobots,
      Integer numberOfRobotsInOrder,
      Integer numberOfUpgradesInOrder) {}

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
