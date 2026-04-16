package io.camunda.demo.dto;

import io.camunda.demo.model.Customer;
import io.camunda.demo.model.Order;
import io.camunda.demo.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Type-safe DTO for a customer in the Camunda Robotics customer database.
 */
public record CustomerDto(
    Long id,
    String name,
    String email,
    AddressDto address,
    PaymentInfoDto paymentInfo,
    List<OrderDto> orders) {

  public record AddressDto(String street, String city, String country) {
  }

  public record PaymentInfoDto(String method, String reference) {
  }

  /**
   * DTO for a single order line item. Either {@code robot} or {@code upgrade} is non-null,
   * depending on what was ordered.
   */
  public record OrderItemDto(RobotDto robot, RobotDto.UpgradeDto upgrade, int quantity) {

    public static OrderItemDto from(OrderItem item) {
      return new OrderItemDto(
          item.getRobot() != null ? RobotDto.from(item.getRobot()) : null,
          item.getUpgrade() != null ? RobotDto.UpgradeDto.from(item.getUpgrade()) : null,
          item.getQuantity());
    }
  }

  public record OrderDto(
      Long id,
      LocalDate orderDate,
      AddressDto shipmentAddress,
      LocalDate shipmentDate,
      LocalDate paymentDate,
      BigDecimal paymentAmount,
      List<OrderItemDto> items) {

    public static OrderDto from(Order order) {
      return new OrderDto(
          order.getId(),
          order.getOrderDate(),
          new AddressDto(
              order.getShipmentAddressStreet(),
              order.getShipmentAddressCity(),
              order.getShipmentAddressCountry()),
          order.getShipmentDate(),
          order.getPaymentDate(),
          order.getPaymentAmount(),
          order.getItems().stream().map(OrderItemDto::from).toList());
    }
  }

  public static CustomerDto from(Customer customer) {
    return new CustomerDto(
        customer.getId(),
        customer.getName(),
        customer.getEmail(),
        new AddressDto(
            customer.getAddressStreet(),
            customer.getAddressCity(),
            customer.getAddressCountry()),
        new PaymentInfoDto(customer.getPaymentMethod(), customer.getPaymentReference()),
        customer.getOrders().stream().map(OrderDto::from).toList());
  }
}
