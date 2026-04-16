package io.camunda.demo;

import io.camunda.client.CamundaClient;
import io.camunda.client.api.response.ProcessInstanceEvent;
import io.camunda.process.test.api.CamundaAssert;
import io.camunda.process.test.api.CamundaProcessTestContext;
import io.camunda.process.test.api.CamundaSpringProcessTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CamundaSpringProcessTest
public class ProcessOrderApplicationTests {

  @Autowired
  private CamundaClient client;

  @Autowired
  private CamundaProcessTestContext processTestContext;

  @Test
  void shouldCompleteProcessInstance() {
    // given
    final ProcessInstanceEvent processInstance = client.newCreateInstanceCommand()
        .bpmnProcessId("ccon-2026-cpt-demo-process")
        .latestVersion()
        .send()
        .join();

    // when - job workers complete the jobs

    // then
    CamundaAssert.assertThat(processInstance).isCompleted();
  }
}

