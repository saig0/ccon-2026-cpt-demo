package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.dto.OrderRequestDto;
import io.camunda.demo.services.OrderDatabaseService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job worker that creates a new order in the Camunda Robotics order database.
 *
 * <p>Receives customer ID, shipment address, payment amount, and order items as input variables,
 * and outputs the created order as an {@code order} variable.
 */
@Component
public class CreateOrderWorker {

  private static final Logger LOG = LoggerFactory.getLogger(CreateOrderWorker.class);

  private final OrderDatabaseService orderDatabaseService;

  public CreateOrderWorker(final OrderDatabaseService orderDatabaseService) {
    this.orderDatabaseService = orderDatabaseService;
  }

  @JobWorker(type = "create-order")
  public Map<String, Object> createOrder(
      final ActivatedJob job, @Variable(name = "orderRequest") final OrderRequestDto orderRequest) {

    LOG.info("Processing create-order job: {}", job.getKey());

    final OrderDto order = orderDatabaseService.createOrder(orderRequest);

    LOG.info("Created order {} for customer {}", order.id(), orderRequest.customerId());
    return Map.of("order", order);
  }
}
