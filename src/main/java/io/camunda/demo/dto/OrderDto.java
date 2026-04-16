package io.camunda.demo.dto;

import io.camunda.demo.model.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
