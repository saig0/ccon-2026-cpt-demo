package io.camunda.demo.integrationTests;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.testCases.TestCase;
import io.camunda.process.test.api.testCases.TestCaseRunner;
import io.camunda.process.test.api.testCases.TestCaseSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * JSON-based integration tests for the customer support agent process using real data services
 * backed by the H2 database pre-loaded with {@code data.sql}.
 *
 * <p>The AI agent connector job worker is mocked using the JSON test case instructions to make the
 * tests deterministic. The send-chat-message job worker is also mocked since no real UI is
 * available.
 */
@ActiveProfiles({"example-data"})
@SpringBootTest
@CamundaSpringProcessTest
public class AgentIntegrationJsonTest {

  @Autowired private TestCaseRunner testCaseRunner;

  @ParameterizedTest
  @TestCaseSource(fileNames = "agent-integration-test.json")
  void runJsonTestCase(final TestCase testCase, final String fileName) {
    testCaseRunner.run(testCase);
  }
}
