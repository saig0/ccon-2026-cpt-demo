package io.camunda.demo.integrationTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatProcessInstance;
import static io.camunda.process.test.api.assertions.ElementSelectors.byId;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.demo.util.CustomerSupportAgentProcessUtil;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

  private static final Logger LOGGER = LoggerFactory.getLogger(AgentIntegrationTest.class);

  private static final Runnable END_CONVERSATION =
      () -> {
        // end the conversation
      };

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  private CustomerSupportAgentProcessUtil processUtil;

  @BeforeEach
  void setup() {
    processUtil =
        new CustomerSupportAgentProcessUtil(client, CustomerSupportAgentProcess.CONVERSATION_ID);
  }

  @BeforeEach
  void mockJobWorkers() {
    // Complete all send chat message jobs
    processTestContext
        .mockJobWorker(CustomerSupportAgentProcess.SEND_CHAT_MESSAGE_JOB_TYPE)
        .withHandler(
            (jobClient, job) -> {
              LOGGER.info(
                  "Send agent message: '{}'",
                  job.getVariable(CustomerSupportAgentProcess.Variables.SEND_CHAT_MESSAGE));

              jobClient.newCompleteCommand(job).send();
            });
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
  @DisplayName("Should offer an upgrade and place an order")
  void shouldOfferUpgradeAndPlaceOrder() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance("Luke", "I have a problem with my robot");

    // when
    processTestContext
        .when(() -> processUtil.awaitUserMessage(processInstance))
        .as("Mock user reply")
        .then(() -> processUtil.publishUserMessage("It's about C3P0. He is talking too much."))
        .then(() -> processUtil.publishUserMessage("Sounds good. Please order it for me."))
        .then(END_CONVERSATION);

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElement(byId(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID), 3);

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(
            byId(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.SEARCH_KNOWLEDGE_BASE_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.ORDER_ITEMS_ELEMENT_ID))
        .hasVariableSatisfiesJudge(
            CustomerSupportAgentProcess.Variables.CONVERSATION,
            """
                      The reply should be friendly and professional. It should contains: \
                      1. A greeting to 'Luke', \
                      2. Ask if the problem is about 'R2-D2' or 'C-3PO', \
                      3. Confirm that this is expected behavior,
                      4. Offer an upgrade to reduce the verbosity,
                      5. Confirm the order of the upgrade.""");
  }

  @Test
  @DisplayName("Should offer a new robot and place an order")
  void shouldOfferNewRobotAndPlaceOrder() {
    // given
    final ProcessInstanceEvent processInstance =
        processUtil.createProcessInstance("Zee", "I'm looking for a friend for my robot");

    // when
    processTestContext
        .when(() -> processUtil.awaitUserMessage(processInstance))
        .as("Mock user reply")
        .then(() -> processUtil.publishUserMessage("EVE sounds like a great match for WALL-E."))
        .then(() -> processUtil.publishUserMessage("Do I get any discount?"))
        .then(() -> processUtil.publishUserMessage("Perfect. Please order it for me."))
        .then(END_CONVERSATION);

    // then
    assertThatProcessInstance(processInstance)
        .withAssertionTimeout(Duration.ofMinutes(2))
        .hasCompletedElement(byId(CustomerSupportAgentProcess.SEND_AGENT_REPLY_ELEMENT_ID), 4);

    assertThatProcessInstance(processInstance)
        .hasCompletedElements(
            byId(CustomerSupportAgentProcess.LOAD_CUSTOMER_DATA_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.CALCULATE_DISCOUNT_ELEMENT_ID),
            byId(CustomerSupportAgentProcess.ORDER_ITEMS_ELEMENT_ID))
        .hasVariableSatisfiesJudge(
            CustomerSupportAgentProcess.Variables.CONVERSATION,
            """
                        The reply should be friendly and professional. It should contains: \
                        1. A greeting to 'Zee', \
                        2. Confirm that Zee has a robot 'WALL-E',
                        3. Propose different robots such as 'EVE', \
                        4. Offer a discount of 15%,
                        5. Confirm the order of the new robot.""");
  }
}
