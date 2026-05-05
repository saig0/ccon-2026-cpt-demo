package io.camunda.demo.unitTests;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.testCases.TestCase;
import io.camunda.process.test.api.testCases.TestCaseRunner;
import io.camunda.process.test.api.testCases.TestCaseSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * JSON-based test cases for the tool calls in the customer support agent process.
 *
 * <p>Job worker invocation is verified via local task variables (BPMN input mappings) rather than
 * service mock assertions.
 */
@SpringBootTest
@CamundaSpringProcessTest
public class AgentToolCallsJsonTest {

  @Autowired private TestCaseRunner testCaseRunner;

  @ParameterizedTest
  @TestCaseSource(fileNames = "agent-tool-calls-test.json")
  void runJsonTestCase(final TestCase testCase, final String fileName) {
    testCaseRunner.run(testCase);
  }
}
