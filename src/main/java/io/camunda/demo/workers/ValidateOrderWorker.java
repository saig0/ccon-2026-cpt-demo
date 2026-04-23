package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.client.exception.BpmnError;
import io.camunda.demo.dto.CustomerDto;
import io.camunda.demo.dto.OrderItemDto;
import io.camunda.demo.model.RobotIntent;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job worker that validates an order against the customer's buy permissions.
 *
 * <p>Checks that:
 * <ul>
 *   <li>The customer is allowed to buy robots ({@code canBuyRobots}).
 *   <li>If the order contains any security robot (intent: {@code GUARD}), the customer must also
 *       have passed the additional compliance check ({@code canBuySecurityRobots}).
 * </ul>
 *
 * <p>Throws a BPMN error ({@code ORDER_NOT_ALLOWED}) when validation fails so that the process
 * can handle the rejection gracefully.
 */
@Component
public class ValidateOrderWorker {

  private static final Logger LOG = LoggerFactory.getLogger(ValidateOrderWorker.class);

  private static final String ERROR_CODE = "ORDER_NOT_ALLOWED";

  @JobWorker(type = "validate-order")
  public void validateOrder(
      final ActivatedJob job,
      @Variable(name = "customer") CustomerDto customer,
      @Variable(name = "orderItems") List<OrderItemDto> orderItems) {

    LOG.info("Processing validate-order job: {}", job.getKey());

    if (!customer.canBuyRobots()) {
      String message = "Customer '%s' is not allowed to buy robots.".formatted(customer.name());
      LOG.warn(message);
      throw new BpmnError(ERROR_CODE, message);
    }

    boolean containsSecurityRobot = orderItems.stream()
        .filter(item -> item.robot() != null)
        .anyMatch(item -> RobotIntent.GUARD == item.robot().intent());

    if (containsSecurityRobot && !customer.canBuySecurityRobots()) {
      String message = ("Customer '%s' is not allowed to buy security robots. "
          + "An additional compliance check is required.").formatted(customer.name());
      LOG.warn(message);
      throw new BpmnError(ERROR_CODE, message);
    }

    LOG.info("Order validation passed for customer '{}'. validate-order completed: {}",
        customer.name(), job.getKey());
  }
}
