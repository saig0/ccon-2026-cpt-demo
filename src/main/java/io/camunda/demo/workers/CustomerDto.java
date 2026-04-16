package io.camunda.demo.workers;

import io.camunda.demo.model.Customer;
import io.camunda.demo.model.Order;
import io.camunda.demo.model.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Type-safe representation of a customer as returned by the customer-data worker.
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

  public record OrderItemDto(String productReference, int quantity) {

    public static OrderItemDto from(OrderItem item) {
      return new OrderItemDto(item.getProductReference(), item.getQuantity());
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
