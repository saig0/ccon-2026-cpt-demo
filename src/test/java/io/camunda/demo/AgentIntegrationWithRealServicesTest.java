package io.camunda.demo;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;
import static io.camunda.process.test.api.assertions.ElementSelectors.byName;
import static java.util.Map.entry;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.assertions.ProcessInstanceSelectors;
import io.camunda.process.test.api.mock.JobWorkerMockBuilder.JobWorkerMock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test for the customer support agent process using a real LLM (AWS Bedrock) and the
 * real data services backed by the H2 database pre-loaded with {@code data.sql}.
 *
 * <p>Requires the environment variables {@code AWS_BEDROCK_ACCESS_KEY} and {@code
 * AWS_BEDROCK_SECRET_KEY} to be set.
 */
@SpringBootTest(
    properties = {
      // Enable the AI connector
      "camunda.process-test.runtime-mode=managed",
      "camunda.process-test.connectors-enabled=true",
      // Set connector secrets for the AI connector
      "camunda.process-test.connectors-secrets.AWS_BEDROCK_ACCESS_KEY=${AWS_BEDROCK_ACCESS_KEY}",
      "camunda.process-test.connectors-secrets.AWS_BEDROCK_SECRET_KEY=${AWS_BEDROCK_SECRET_KEY}",
      // Configure the judge for assertions
      "camunda.process-test.judge.chat-model.provider=amazon-bedrock",
      "camunda.process-test.judge.chat-model.model=eu.anthropic.claude-haiku-4-5-20251001-v1:0",
      "camunda.process-test.judge.chat-model.region=eu-central-1",
      "camunda.process-test.judge.chat-model.credentials.access-key=${AWS_BEDROCK_ACCESS_KEY}",
      "camunda.process-test.judge.chat-model.credentials.secret-key=${AWS_BEDROCK_SECRET_KEY}",
      // Load example data
      "spring.sql.init.mode=always",
      "spring.jpa.hibernate.ddl-auto=none",
      "spring.datasource.url=jdbc:h2:mem:product-catalog;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
      "spring.datasource.driver-class-name=org.h2.Driver",
    })
@CamundaSpringProcessTest
public class AgentIntegrationWithRealServicesTest {

  private static final String CONVERSATION_ID = "conversation-hiro-real-1";
  private static final String MESSAGE_NAME = "user-message";

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  private JobWorkerMock sendChatMessageMock;

  @RegisterExtension
  private final ConversationLogger conversationLogger =
      new ConversationLogger(
          () ->
              sendChatMessageMock.getActivatedJobs().stream()
                  .map(job -> (String) job.getVariable("message"))
                  .toList());

  @BeforeEach
  void setupMocks() {
    sendChatMessageMock = processTestContext.mockJobWorker("send-chat-message").thenComplete();
  }

  private static final class ConversationLogger implements TestWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationLogger.class);

    private final Supplier<List<String>> conversationLogsSupplier;

    private ConversationLogger(Supplier<List<String>> conversationLogsSupplier) {
      this.conversationLogsSupplier = conversationLogsSupplier;
    }

    @Override
    public void testFailed(ExtensionContext context, @Nullable Throwable cause) {
      final String messages =
          String.join(
              "\n=======================================\n", conversationLogsSupplier.get());
      LOGGER.info(
          "Test failed. Dumping conversation logs: \n=======================================\n{}",
          messages);
    }
  }

  @Test
  void shouldResolveProblem() {
    // given
    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("userName", "Hiro"),
                    entry("message", "My robot is losing air"),
                    entry("conversationId", CONVERSATION_ID)))
            .send()
            .join();

    // when
    processTestContext
        .when(
            () ->
                assertThatProcessInstance(
                        ProcessInstanceSelectors.byProcessId(
                            CustomerSupportAgentProcess.PROCESS_ID))
                    .isWaitingForMessage(MESSAGE_NAME, CONVERSATION_ID))
        .as("Mock user reply")
        .then(
            () ->
                client
                    .newPublishMessageCommand()
                    .messageName("user-message")
                    .correlationKey(CONVERSATION_ID)
                    .variables(Map.of("message", "Thank you, that fixed it!"))
                    .send()
                    .join());

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
            byId("analyze-conversation"));

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byId("load-customer-data"), byId("search-knowledge-base"))
        .hasVariableSatisfiesJudge(
            "conversation",
            """
                      The reply should be friendly and professional. It should contains: \
                      1. A greeting to 'Hiro', \
                      2. Confirm that the issue is about Baymax, \
                      3. Propose a solution using a tape.""");
  }

  @Test
  void shouldOfferUpgrade() {
    // given
    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("userName", "Luke"),
                    entry("message", "I have a problem with my robot"),
                    entry("conversationId", CONVERSATION_ID)))
            .send()
            .join();

    // when
    processTestContext
        .when(
            () ->
                assertThatProcessInstance(
                        ProcessInstanceSelectors.byProcessId(
                            CustomerSupportAgentProcess.PROCESS_ID))
                    .isWaitingForMessage(MESSAGE_NAME, CONVERSATION_ID))
        .as("Mock user reply")
        .then(
            () ->
                client
                    .newPublishMessageCommand()
                    .messageName("user-message")
                    .correlationKey(CONVERSATION_ID)
                    .variables(Map.of("message", "It's about C3P0. He is talking too much."))
                    .send()
                    .join())
        .then(
            () ->
                client
                    .newPublishMessageCommand()
                    .messageName("user-message")
                    .correlationKey(CONVERSATION_ID)
                    .variables(Map.of("message", "Perfect. I want to have this upgrade."))
                    .send()
                    .join())
        .then(
            () -> {
              // end the conversation loop after the upgrade is offered
            });

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElement(byName("Send agent reply"), 3);

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byId("load-customer-data"), byId("search-knowledge-base"))
        .hasVariableSatisfiesJudge(
            "conversation",
            """
                      The reply should be friendly and professional. It should contains: \
                      1. A greeting to 'Luke', \
                      2. Ask if the problem is about 'R2-D2' or 'C-3PO', \
                      3. Confirm that this is expected behavior,
                      4. Offer an upgrade to reduce the verbosity.""");
  }

  @Disabled
  @Test
  void dynamicConversation() {
    // given
    final ProcessInstanceEvent processInstance =
        client
            .newCreateInstanceCommand()
            .bpmnProcessId(CustomerSupportAgentProcess.PROCESS_ID)
            .latestVersion()
            .variables(
                Map.ofEntries(
                    entry("userName", "Hiro"),
                    entry("message", "My robot is losing air"),
                    entry("conversationId", CONVERSATION_ID)))
            .send()
            .join();

    // when
    processTestContext
        .when(
            () ->
                assertThatProcessInstance(
                        ProcessInstanceSelectors.byProcessId(
                            CustomerSupportAgentProcess.PROCESS_ID))
                    .isWaitingForMessage(MESSAGE_NAME, CONVERSATION_ID))
        .as("Mock user reply")
        .then(
            () -> {
              final String lastAgentReply =
                  sendChatMessageMock
                      .getActivatedJobs()
                      .getLast()
                      .getVariable("message")
                      .toString();

              final String userReply =
                  lastAgentReply.toLowerCase().contains("tape")
                      ? "Thank you, that fixed it!"
                      : "That didn't work, I'm still having the issue.";

              client
                  .newPublishMessageCommand()
                  .messageName("user-message")
                  .correlationKey(CONVERSATION_ID)
                  .variables(Map.of("message", userReply))
                  .send()
                  .join();
            });

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElementsInOrder(
            byId(CustomerSupportAgentProcess.AD_HOC_SUB_PROCESS_ELEMENT_ID),
            byId("analyze-conversation"));

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(byId("load-customer-data"), byId("search-knowledge-base"))
        .hasLocalVariableSatisfiesJudge(
            byId("send-agent-reply"),
            "message",
            """
                                  The reply should be friendly and professional. It should contains: \
                                  1. a greeting to 'Hiro', \
                                  2. confirm that the issue is about Baymax, \
                                  3. propose a solution using a tape.""");
  }
}
