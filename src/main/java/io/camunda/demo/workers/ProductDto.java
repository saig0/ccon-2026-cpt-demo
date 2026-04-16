package io.camunda.demo.workers;

import io.camunda.demo.model.Robot;
import io.camunda.demo.model.RobotIntent;
import io.camunda.demo.model.Upgrade;

import java.math.BigDecimal;
import java.util.List;

/**
 * Type-safe representation of a robot product as returned by the catalog worker.
 */
public record ProductDto(
    Long id,
    String modelId,
    String modelVersion,
    String name,
    String description,
    RobotIntent intent,
    BigDecimal price,
    List<UpgradeDto> compatibleUpgrades) {

  /**
   * Type-safe representation of an upgrade as returned alongside its parent robot.
   */
  public record UpgradeDto(Long id, String name, BigDecimal price) {

    public static UpgradeDto from(Upgrade upgrade) {
      return new UpgradeDto(upgrade.getId(), upgrade.getName(), upgrade.getPrice());
    }
  }

  public static ProductDto from(Robot robot) {
    return new ProductDto(
        robot.getId(),
        robot.getModelId(),
        robot.getModelVersion(),
        robot.getName(),
        robot.getDescription(),
        robot.getIntent(),
        robot.getPrice(),
        robot.getCompatibleUpgrades().stream().map(UpgradeDto::from).toList());
  }
}
