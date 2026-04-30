package io.camunda.demo.dto;

import java.math.BigDecimal;
import java.util.List;

/** Input DTO for creating an order. */
public record OrderRequestDto(
    Long customerId,
    AddressDto shipmentAddress,
    BigDecimal paymentAmount,
    List<OrderItemInputDto> orderItems) {}
