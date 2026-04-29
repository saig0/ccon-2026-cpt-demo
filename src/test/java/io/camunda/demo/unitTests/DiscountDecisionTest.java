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
    "0, 0", // No discount
    "1, 15", // Loyalty: 1+ robot
    "2, 15", //
    "3, 20", // Loyalty: 3+ robots
    "4, 20", //
    "5, 30", // Loyalty: 5+ robots
    "10, 30", //
  })
  void shouldCalculateDiscount(
      final Integer customerPreviousRobotCount, final Integer expectedDiscount) {
    // given
    final CustomerSupportAgentProcess.DiscountDecisionInput decisionInput =
        new CustomerSupportAgentProcess.DiscountDecisionInput(customerPreviousRobotCount, 1, 0);

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
