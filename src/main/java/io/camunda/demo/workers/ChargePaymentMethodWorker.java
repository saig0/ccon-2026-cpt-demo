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
 * Job worker that charges the payment method for an order by setting its payment date.
 *
 * <p>Receives the current {@code order} as an input variable, updates the payment date in the
 * database, and outputs the updated order as an {@code order} variable.
 */
@Component
public class ChargePaymentMethodWorker {

  private static final Logger LOG = LoggerFactory.getLogger(ChargePaymentMethodWorker.class);

  private final OrderDatabaseService orderDatabaseService;

  public ChargePaymentMethodWorker(OrderDatabaseService orderDatabaseService) {
    this.orderDatabaseService = orderDatabaseService;
  }

  @JobWorker(type = "charge-payment-method")
  public Map<String, Object> chargePaymentMethod(
      final ActivatedJob job, @Variable(name = "order") OrderDto order) {

    LOG.info("Processing charge-payment-method job: {}", job.getKey());

    final OrderDto updatedOrder = orderDatabaseService.chargePaymentMethod(order.id());

    LOG.info("Charged payment for order {}", updatedOrder.id());
    return Map.of("order", updatedOrder);
  }
}
