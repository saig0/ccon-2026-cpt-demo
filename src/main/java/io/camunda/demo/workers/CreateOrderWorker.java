package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.dto.OrderItemInput;
import io.camunda.demo.services.OrderDatabaseService;
import java.math.BigDecimal;
import java.util.List;
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

  public CreateOrderWorker(OrderDatabaseService orderDatabaseService) {
    this.orderDatabaseService = orderDatabaseService;
  }

  @JobWorker(type = "create-order")
  public Map<String, Object> createOrder(
      final ActivatedJob job,
      @Variable(name = "customerId") Long customerId,
      @Variable(name = "shippmentAddress") AddressDto shippmentAddress,
      @Variable(name = "paymentAmount") BigDecimal paymentAmount,
      @Variable(name = "orderItems") List<OrderItemInput> orderItems) {

    LOG.info("Processing create-order job: {}", job.getKey());

    final OrderDto order =
        orderDatabaseService.createOrder(customerId, shippmentAddress, paymentAmount, orderItems);

    LOG.info("Created order {} for customer {}", order.id(), customerId);
    return Map.of("order", order);
  }
}
