package io.camunda.demo.dto;

import io.camunda.demo.model.Robot;
import io.camunda.demo.model.RobotIntent;
import java.math.BigDecimal;
import java.util.List;

/** Type-safe DTO for a robot in the Camunda Robotics product catalog. */
public record RobotDto(
    Long id,
    String modelId,
    String modelVersion,
    String name,
    String description,
    RobotIntent intent,
    BigDecimal price,
    List<UpgradeDto> compatibleUpgrades) {

  public static RobotDto from(Robot robot) {
    return new RobotDto(
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
