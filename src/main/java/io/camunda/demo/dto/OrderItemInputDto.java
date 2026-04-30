package io.camunda.demo.dto;

/**
 * Input DTO for a single line item when creating an order. Exactly one of {@code robotId} or {@code
 * upgradeId} should be non-null.
 */
public record OrderItemInputDto(Long robotId, Long upgradeId, int quantity) {}
