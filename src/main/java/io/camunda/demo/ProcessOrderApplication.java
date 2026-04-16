package io.camunda.demo;

import io.camunda.client.annotation.Deployment;
import io.camunda.demo.model.Robot;
import io.camunda.demo.services.ProductCatalogService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
@Deployment(resources = "classpath*:/bpmn/**/*.bpmn")
public class ProcessOrderApplication {

  private static final Logger LOGGER =
      org.slf4j.LoggerFactory.getLogger(ProcessOrderApplication.class);

  @Autowired private ProductCatalogService productCatalogService;

  public static void main(String[] args) {
    SpringApplication.run(ProcessOrderApplication.class, args);
  }

  @PostConstruct
  void printDatabaseEntriesForDebugging() {
    List<Robot> robots = productCatalogService
            .findAllRobots();
    LOGGER.debug("Available robots in the product catalog: {}", robots.size());
    robots
        .forEach(
            robot ->
                LOGGER.debug(
                    "Robot: {} (model: {}, intent: {})",
                    robot.getName(),
                    robot.getModelId(),
                    robot.getIntent()));
  }
}
