package io.camunda.demo.services;

import io.camunda.demo.dto.AddressDto;
import io.camunda.demo.dto.OrderDto;
import io.camunda.demo.dto.OrderItemInputDto;
import io.camunda.demo.dto.OrderRequestDto;
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
import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing orders in the Camunda Robotics order database. Returns {@link OrderDto}
 * objects instead of JPA entities to avoid lazy-loading issues outside the transaction boundary.
 */
@Service
@Transactional
public class OrderDatabaseService {

  private final OrderRepository orderRepository;
  private final CustomerRepository customerRepository;
  private final RobotRepository robotRepository;
  private final UpgradeRepository upgradeRepository;

  public OrderDatabaseService(
      final OrderRepository orderRepository,
      final CustomerRepository customerRepository,
      final RobotRepository robotRepository,
      final UpgradeRepository upgradeRepository) {
    this.orderRepository = orderRepository;
    this.customerRepository = customerRepository;
    this.robotRepository = robotRepository;
    this.upgradeRepository = upgradeRepository;
  }

  /**
   * Creates a new order for the given customer with the provided items.
   *
   * @param orderRequest the request DTO
   * @return the created order as a DTO
   */
  public OrderDto createOrder(final OrderRequestDto orderRequest) {

    if (orderRequest == null) {
      throw new IllegalArgumentException("orderRequest must not be null");
    }

    final Customer customer =
        customerRepository
            .findById(orderRequest.customerId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Customer not found with id: " + orderRequest.customerId()));

    final AddressDto shipmentAddress = orderRequest.shipmentAddress();

    final Order order =
        new Order(
            customer,
            LocalDate.now(),
            shipmentAddress.street(),
            shipmentAddress.city(),
            shipmentAddress.country(),
            orderRequest.paymentAmount());

    for (final OrderItemInputDto input : orderRequest.orderItems()) {
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
   * Confirms payment for the given order by setting the payment date to today and advancing the
   * status to {@link OrderStatus#PAID}.
   *
   * @param orderId the ID of the order to charge
   * @return the updated order as a DTO
   */
  public OrderDto chargePaymentMethod(final Long orderId) {
    final Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

    order.setPaymentDate(LocalDate.now());
    order.setStatus(OrderStatus.PAID);
    return OrderDto.from(orderRepository.save(order));
  }

  /**
   * Prepares shipping for the given order by setting an estimated delivery date (7 days from today)
   * and advancing the status to {@link OrderStatus#PREPARED_FOR_SHIPPING}.
   *
   * @param orderId the ID of the order to prepare for shipping
   * @return the updated order as a DTO
   */
  public OrderDto prepareShipping(final Long orderId) {
    final Order order =
        orderRepository
            .findById(orderId)
            .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + orderId));

    order.setEstimatedDeliveryDate(LocalDate.now().plusDays(7));
    order.setStatus(OrderStatus.PREPARED_FOR_SHIPPING);
    return OrderDto.from(orderRepository.save(order));
  }

  private static boolean isBlank(final String value) {
    return value == null || value.isBlank();
  }
}
