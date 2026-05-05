package io.camunda.demo.integrationTests;

import io.camunda.process.test.api.CamundaSpringProcessTest;
import io.camunda.process.test.api.testCases.TestCase;
import io.camunda.process.test.api.testCases.TestCaseRunner;
import io.camunda.process.test.api.testCases.TestCaseSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

/**
 * JSON-based integration tests for the customer support agent process using a real LLM (AWS
 * Bedrock) and the * real data services backed by the H2 database pre-loaded with {@code data.sql}.
 * The tests are counterparts of the Java-based integration test.
 *
 * <p>Requires the environment variables {@code AWS_BEDROCK_ACCESS_KEY} and {@code *
 * AWS_BEDROCK_SECRET_KEY} to be set.
 */
@ActiveProfiles({"integration-test", "example-data"})
@SpringBootTest(properties = {"camunda.process-test.assertion.timeout=PT2M"})
@CamundaSpringProcessTest
@DirtiesContext // Clean the database after all tests
public class AgentIntegrationJsonTest {

  @Autowired private TestCaseRunner testCaseRunner;

  @ParameterizedTest(name = "[{index}] {0} ({1})")
  @TestCaseSource(directory = "/test-cases/integration-tests")
  @DisplayName("Run JSON-based integration test case for customer support agent process")
  void runJsonTestCase(final TestCase testCase, final String fileName) {
    // when/then: run and verify the test case
    testCaseRunner.run(testCase);
  }
}
