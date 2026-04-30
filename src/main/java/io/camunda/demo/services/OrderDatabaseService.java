package io.camunda.demo.services;

import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.dto.OrderItemInput;
import io.camunda.demo.model.Customer;
import io.camunda.demo.model.Order;
import io.camunda.demo.model.OrderItem;
import io.camunda.demo.model.OrderStatus;
import io.camunda.demo.model.Robot;
import io.camunda.demo.model.Upgrade;
import io.camunda.demo.repositories.CustomerRepository;
import io.camunda.demo.repositories.OrderRepository;
import io.camunda.demo.repositories.RobotRepository;
import io.camunda.demo.repositories.UpgradeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing orders in the Camunda Robotics order database.
 * Returns {@link OrderDto} objects instead of JPA entities to avoid lazy-loading issues
 * outside the transaction boundary.
 */
@Service
@Transactional
public class OrderDatabaseService {

  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RobotRepository robotRepository;
  private final UpgradeRepository upgradeRepository;

  public OrderDatabaseService(
      OrderRepository orderRepository,
      CustomerRepository customerRepository,
      RobotRepository robotRepository,
      UpgradeRepository upgradeRepository) {
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.robotRepository = robotRepository;
    this.upgradeRepository = upgradeRepository;
  }

  /**
   * Creates a new order for the given customer with the provided items.
   *
   * @param customerId      the ID of the customer placing the order
   * @param shipmentAddress the destination address for the shipment
   * @param paymentAmount   the total payment amount in €
   * @param orderItems      the line items of the order
   * @return the created order as a DTO
   */
  public OrderDto createOrder(
      Long customerId,
      AddressDto shipmentAddress,
      BigDecimal paymentAmount,
      List<OrderItemInput> orderItems) {

    if (customerId == null) {
      throw new IllegalArgumentException("customerId must not be null");
    }
    if (shipmentAddress == null) {
      throw new IllegalArgumentException("shipmentAddress must not be null");
    }
    if (isBlank(shipmentAddress.street())) {
      throw new IllegalArgumentException("shipmentAddress.street must not be blank");
    }
    if (isBlank(shipmentAddress.city())) {
      throw new IllegalArgumentException("shipmentAddress.city must not be blank");
    }
    if (isBlank(shipmentAddress.country())) {
      throw new IllegalArgumentException("shipmentAddress.country must not be blank");
    }
    if (paymentAmount == null) {
      throw new IllegalArgumentException("paymentAmount must not be null");
    }
    if (paymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("paymentAmount must be greater than zero");
    }
    if (orderItems == null || orderItems.isEmpty()) {
      throw new IllegalArgumentException("orderItems must not be null or empty");
    }
    for (final OrderItemInput item : orderItems) {
      if (item.robotId() == null && item.upgradeId() == null) {
        throw new IllegalArgumentException("Each order item must have a robotId or upgradeId");
      }
      if (item.quantity() <= 0) {
        throw new IllegalArgumentException("Each order item must have a positive quantity");
      }
    }

    final Customer customer =
        customerRepository
            .findById(customerId)
            .orElseThrow(
                () -> new IllegalArgumentException("Customer not found with id: " + customerId));

    final Order order =
        new Order(
            customer,
            LocalDate.now(),
            shipmentAddress.street(),
            shipmentAddress.city(),
            shipmentAddress.country(),
            paymentAmount);

    for (final OrderItemInput input : orderItems) {
      final OrderItem item;
      if (input.robotId() != null) {
        final Robot robot =
            robotRepository
                .findById(input.robotId())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Robot not found with id: " + input.robotId()));
        item = new OrderItem(order, robot, input.quantity());
      } else {
        final Upgrade upgrade =
            upgradeRepository
                .findById(input.upgradeId())
                .orElseThrow(
                    () ->
                        new IllegalArgumentException(
                            "Upgrade not found with id: " + input.upgradeId()));
        item = new OrderItem(order, upgrade, input.quantity());
      }
      order.getItems().add(item);
    }

    return OrderDto.from(orderRepository.save(order));
  }

  /**
   * Confirms payment for the given order by setting the payment date to today
   * and advancing the status to {@link OrderStatus#PAID}.
   *
   * @param orderId the ID of the order to charge
   * @return the updated order as a DTO
   */
  public OrderDto chargePaymentMethod(Long orderId) {
    final Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new IllegalArgumentException("Order not found with id: " + orderId));

    order.setPaymentDate(LocalDate.now());
    order.setStatus(OrderStatus.PAID);
    return OrderDto.from(orderRepository.save(order));
  }

  /**
   * Prepares shipping for the given order by setting an estimated delivery date
   * (7 days from today) and advancing the status to
   * {@link OrderStatus#PREPARED_FOR_SHIPPING}.
   *
   * @param orderId the ID of the order to prepare for shipping
   * @return the updated order as a DTO
   */
  public OrderDto prepareShipping(Long orderId) {
    final Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(
                () -> new IllegalArgumentException("Order not found with id: " + orderId));

    order.setEstimatedDeliveryDate(LocalDate.now().plusDays(7));
    order.setStatus(OrderStatus.PREPARED_FOR_SHIPPING);
    return OrderDto.from(orderRepository.save(order));
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
