package io.camunda.demo.unitTests;

import static io.camunda.process.test.api.CamundaAssert.assertThatDecision;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.EvaluateDecisionResponse;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import java.util.Map;
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
    "0, 0", //
    "1, 10", //
    "2, 10", //
    "3, 15", //
    "4, 15", //
    "5, 25", //
    "10, 25", //
  })
  void shouldCalculateDiscount(final int customerPreviousRobotCount, final int expectedDiscount) {
    // when
    final EvaluateDecisionResponse evaluateDecisionResponse =
        client
            .newEvaluateDecisionCommand()
            .decisionId("discount")
            .variables(Map.of("customerPreviousRobotCount", customerPreviousRobotCount))
            .send()
            .join();

    // then
    assertThatDecision(evaluateDecisionResponse).hasOutput(expectedDiscount);
  }
}
