package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatDecision;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"camunda.client.worker.defaults.enabled=false"})
@CamundaSpringProcessTest
public class DiscountDecisionTest {

  @Autowired private CamundaClient client;
  @Autowired private CamundaProcessTestContext processTestContext;

  @ParameterizedTest
  @CsvSource({
    // Purchased robots | Discount
    "0, 1, 0, 0", // No discount
    "1, 1, 0, 15", // Loyalty: 1+ robot
    "2, 1, 0, 15", //
    "3, 1, 0, 20", // Loyalty: 3+ robots
    "4, 1, 0, 20", //
    "5, 1, 0, 30", // Loyalty: 5+ robots
    "10, 1, 0, 30", //
  })
  void shouldCalculateDiscount(
      final int numberOfPreviouslyPurchasedRobots,
      final int numberOfRobotsInOrder,
      final int numberOfUpgradesInOrder,
      final int expectedDiscount) {
    // given
    final CustomerSupportAgentProcess.DiscountDecisionInput decisionInput =
        new CustomerSupportAgentProcess.DiscountDecisionInput(
            numberOfPreviouslyPurchasedRobots, numberOfRobotsInOrder, numberOfUpgradesInOrder);

    // when
    final EvaluateDecisionResponse evaluateDecisionResponse =
        client
            .newEvaluateDecisionCommand()
            .decisionId(CustomerSupportAgentProcess.DISCOUNT_DECISION_ID)
            .variables(decisionInput)
            .send()
            .join();

    // then
    assertThatDecision(evaluateDecisionResponse).hasOutput(expectedDiscount);
  }
}
