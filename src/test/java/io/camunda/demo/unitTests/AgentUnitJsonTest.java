package io.camunda.demo.unitTests;

import io.camunda.demo.dto.*;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.testCases.TestCase;
import io.camunda.process.test.api.testCases.TestCaseRunner;
import io.camunda.process.test.api.testCases.TestCaseSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JSON-based unit tests for the customer support agent process. The tests are counterparts of the
 * Java-based unit tests.
 */
@SpringBootTest(properties = {"camunda.client.worker.defaults.enabled=false"})
@CamundaSpringProcessTest
public class AgentUnitJsonTest {

  @Autowired private TestCaseRunner testCaseRunner;

  @ParameterizedTest(name = "[{index}] {0} ({1})")
  @TestCaseSource(directory = "/test-cases/unit-tests")
  @DisplayName("Run JSON-based test case for customer support agent process")
  void runJsonTestCase(final TestCase testCase, final String fileName) {
    // when/then: run and verify the test case
    testCaseRunner.run(testCase);
  }
}
