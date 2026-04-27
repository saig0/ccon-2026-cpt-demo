package io.camunda.demo.util;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.CustomerSupportAgentProcess;

public class CustomerSupportAgentProcessUtil {

  private final CamundaClient camundaClient;
  private final String conversationId;

  public CustomerSupportAgentProcessUtil(CamundaClient camundaClient, String conversationId) {
    this.camundaClient = camundaClient;
    this.conversationId = conversationId;
  }

  public ProcessInstanceEvent createProcessInstance(final String userName, final String message) {
    return CustomerSupportAgentProcess.createProcessInstance(
        camundaClient, userName, message, conversationId);
  }

  public void publishUserMessage(final String message) {
    CustomerSupportAgentProcess.publishUserMessage(camundaClient, message, conversationId);
  }

  public void awaitUserMessage(final ProcessInstanceEvent processInstance) {
    CustomerSupportAgentProcess.awaitUserMessage(processInstance, conversationId);
  }
}
