package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatDecision;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.demo.util.CustomerSupportAgentProcess;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"camunda.client.worker.defaults.enabled=false"})
@CamundaSpringProcessTest
public class DiscountDecisionTest {

  @Autowired private CamundaClient client;

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      useHeadersInDisplayName = true,
      textBlock =
          """
          Purchased Robots | Robots in Order | Upgrades in Order | Discount in % |
                 10        |         1       |         0         |       25      | # Loyalty: 5+ robots
                  5        |         1       |         0         |       25      | # Loyalty: 5+ robots
                  4        |         1       |         0         |       20      | # Loyalty: 3+ robots
                  3        |         1       |         0         |       20      | # Loyalty: 3+ robots
                  2        |         1       |         0         |       15      | # Loyalty: 1+ robots
                  1        |         1       |         1         |       15      | # Loyalty: 1+ robots
                  0        |         2       |         2         |       10      | # Bundle: Robot + upgrade
                  0        |         1       |         1         |       10      | # Bundle: Robot + upgrade
                  0        |         0       |         5         |       10      | # Bundle: 4+ upgrades
                  0        |         0       |         4         |       10      | # Bundle: 4+ upgrades
                  0        |         0       |         3         |        5      | # Bundle: 2+ upgrades
                  0        |         0       |         2         |        5      | # Bundle: 2+ upgrades
                  0        |         1       |         0         |        0      | # First robot
                  0        |         0       |         1         |        0      | # Upgrade only
                  0        |         0       |         0         |        0      | # No discount
          """)
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
