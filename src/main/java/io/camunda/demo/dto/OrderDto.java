package io.camunda.demo.dto;

import io.camunda.demo.model.Order;
import io.camunda.demo.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(
    Long id,
    LocalDate orderDate,
    AddressDto shipmentAddress,
    LocalDate estimatedDeliveryDate,
    LocalDate paymentDate,
    BigDecimal paymentAmount,
    List<OrderItemDto> items,
    OrderStatus status) {

  public static OrderDto from(Order order) {
    return new OrderDto(
        order.getId(),
        order.getOrderDate(),
        new AddressDto(
            order.getShipmentAddressStreet(),
            order.getShipmentAddressCity(),
            order.getShipmentAddressCountry()),
        order.getEstimatedDeliveryDate(),
        order.getPaymentDate(),
        order.getPaymentAmount(),
        order.getItems().stream().map(OrderItemDto::from).toList(),
        order.getStatus());
  }
}
