package io.camunda.demo.integrationTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.demo.util.CustomerSupportAgentProcessUtil;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.mock.JobWorkerMockBuilder.JobWorkerMock;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test for the customer support agent process using a real LLM (AWS Bedrock) and the
 * real data services backed by the H2 database pre-loaded with {@code data.sql}.
 *
 * <p>Requires the environment variables {@code AWS_BEDROCK_ACCESS_KEY} and {@code
 * AWS_BEDROCK_SECRET_KEY} to be set.
 */
@ActiveProfiles({"integration-test", "example-data"})
@SpringBootTest
@CamundaSpringProcessTest
public class AgentIntegrationTest {

  private static final Runnable END_CONVERSATION =
      () -> {
        // end the conversation
      };

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  private CustomerSupportAgentProcessUtil processUtil;

  private JobWorkerMock sendChatMessageMock;

  @RegisterExtension
  private final ConversationLogger conversationLogger =
      new ConversationLogger(() -> sendChatMessageMock);

  @BeforeEach
  void setup() {
    processUtil =
        new CustomerSupportAgentProcessUtil(client, CustomerSupportAgentProcess.CONVERSATION_ID);
  }

  @BeforeEach
  void mockJobWorkers() {
    // Complete all send chat message jobs
    sendChatMessageMock =
        processTestContext
            .mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE)
            .thenComplete();
  }

  @Test
  @DisplayName("Should identify the customer and solve the problem using the knowledge base")
  void shouldResolveProblem() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance("Hiro", "My robot is losing air");

    // when
    processTestContext
        .when(() -> processUtil.awaitUserMessage(processInstance))
        .as("Mock user reply")
        .then(() -> processUtil.publishUserMessage("Thank you, that fixed it!"))
        .then(END_CONVERSATION);

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElement(byId(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID), 2);

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(
            byId(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID))
        .hasVariableSatisfiesJudge(
            CustomerSupportAgentProcess.Variables.CONVERSATION,
            """
                      The reply should be friendly and professional. It should contains: \
                      1. A greeting to 'Hiro', \
                      2. Confirm that the issue is about Baymax, \
                      3. Propose a solution using a tape.""");
  }

  @Test
  @DisplayName("Should offer an upgrade to reduce the verbosity if the problem is about C-3PO")
  void shouldOfferUpgrade() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance("Luke", "I have a problem with my robot");

    // when
    processTestContext
        .when(() -> processUtil.awaitUserMessage(processInstance))
        .as("Mock user reply")
        .then(() -> processUtil.publishUserMessage("It's about C3P0. He is talking too much."))
        .then(() -> processUtil.publishUserMessage("Perfect. I want to have this upgrade."))
        .then(END_CONVERSATION);

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElement(byId(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID), 3);

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(
            byId(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID))
        .hasVariableSatisfiesJudge(
            CustomerSupportAgentProcess.Variables.CONVERSATION,
            """
                      The reply should be friendly and professional. It should contains: \
                      1. A greeting to 'Luke', \
                      2. Ask if the problem is about 'R2-D2' or 'C-3PO', \
                      3. Confirm that this is expected behavior,
                      4. Offer an upgrade to reduce the verbosity.""");
  }

  private static final class ConversationLogger implements TestWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConversationLogger.class);

    private static final String DELIMITER = "\n=======================================\n";

    private final Supplier<JobWorkerMock> sendChatMessageMockSupplier;

    private ConversationLogger(final Supplier<JobWorkerMock> sendChatMessageMockSupplier) {
      this.sendChatMessageMockSupplier = sendChatMessageMockSupplier;
    }

    @Override
    public void testFailed(final ExtensionContext context, @Nullable final Throwable cause) {
      final List<String> messages =
          sendChatMessageMockSupplier.get().getActivatedJobs().stream()
              .map(
                  job ->
                      (String)
                          job.getVariable(CustomerSupportAgentProcess.Variables.SEND_CHAT_MESSAGE))
              .toList();

      LOGGER.info(
          "Test failed. Dumping conversation logs: (size: {}){}{}",
          messages.size(),
          DELIMITER,
          String.join(DELIMITER, messages));
    }
  }
}
