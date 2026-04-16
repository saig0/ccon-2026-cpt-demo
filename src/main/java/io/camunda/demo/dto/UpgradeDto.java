package io.camunda.demo.dto;

import io.camunda.demo.model.Upgrade;
import java.math.BigDecimal;

/**
 * Type-safe DTO for an upgrade as returned alongside its parent robot. Includes {@code description}
 * (not present in the legacy worker DTO) to give consumers full upgrade details without needing a
 * separate lookup.
 */
public record UpgradeDto(Long id, String name, String description, BigDecimal price) {

  public static UpgradeDto from(Upgrade upgrade) {
    return new UpgradeDto(
        upgrade.getId(), upgrade.getName(), upgrade.getDescription(), upgrade.getPrice());
  }
}
