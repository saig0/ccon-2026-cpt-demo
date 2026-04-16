package io.camunda.demo.dto;

import io.camunda.demo.model.OrderItem;

/**
 * DTO for a single order line item. Either {@code robot} or {@code upgrade} is non-null, depending
 * on what was ordered.
 */
public record OrderItemDto(RobotDto robot, UpgradeDto upgrade, int quantity) {

  public static OrderItemDto from(OrderItem item) {
    return new OrderItemDto(
        item.getRobot() != null ? RobotDto.from(item.getRobot()) : null,
        item.getUpgrade() != null ? UpgradeDto.from(item.getUpgrade()) : null,
        item.getQuantity());
  }
}
