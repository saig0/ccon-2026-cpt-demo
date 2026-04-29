package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.services.OrderDatabaseService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job worker that prepares shipping for an order by setting its shipment date.
 *
 * <p>Receives the current {@code order} as an input variable, updates the shipment date in the
 * database, and outputs the updated order as an {@code order} variable.
 */
@Component
public class PrepareShippingWorker {

  private static final Logger LOG = LoggerFactory.getLogger(PrepareShippingWorker.class);

  private final OrderDatabaseService orderDatabaseService;

  public PrepareShippingWorker(OrderDatabaseService orderDatabaseService) {
    this.orderDatabaseService = orderDatabaseService;
  }

  @JobWorker(type = "prepare-shipping")
  public Map<String, Object> prepareShipping(
      final ActivatedJob job, @Variable(name = "order") OrderDto order) {

    LOG.info("Processing prepare-shipping job: {}", job.getKey());

    final OrderDto updatedOrder = orderDatabaseService.prepareShipping(order.id());

    LOG.info("Prepared shipping for order {}", updatedOrder.id());
    return Map.of("order", updatedOrder);
  }
}
