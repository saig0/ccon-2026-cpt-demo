package io.camunda.demo.services;

import io.camunda.demo.dto.RobotDto;
import io.camunda.demo.dto.UpgradeDto;
import io.camunda.demo.model.RobotIntent;
import io.camunda.demo.repositories.RobotRepository;
import io.camunda.demo.repositories.UpgradeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading and querying the Camunda Robotics product catalog. Returns {@link RobotDto}
 * and {@link UpgradeDto} objects instead of JPA entities to avoid lazy-loading issues outside the
 * transaction boundary.
 */
@Service
@Transactional(readOnly = true)
public class ProductCatalogService {

  private final RobotRepository robotRepository;
  private final UpgradeRepository upgradeRepository;

  public ProductCatalogService(
      RobotRepository robotRepository, UpgradeRepository upgradeRepository) {
    this.robotRepository = robotRepository;
    this.upgradeRepository = upgradeRepository;
  }

  /** Returns all robots in the catalog as DTOs. */
  public List<RobotDto> findAllRobots() {
    return robotRepository.findAll().stream().map(RobotDto::from).toList();
  }

  /** Returns all robots that match a given intent/category as DTOs. */
  public List<RobotDto> findRobotsByIntent(RobotIntent intent) {
    return robotRepository.findByIntent(intent).stream().map(RobotDto::from).toList();
  }

  /** Returns all versions of a robot model as DTOs. */
  public List<RobotDto> findRobotsByModelId(String modelId) {
    return robotRepository.findByModelId(modelId).stream().map(RobotDto::from).toList();
  }

  /** Returns a single robot by its unique ID as a DTO. */
  public Optional<RobotDto> findRobotById(Long id) {
    return robotRepository.findById(id).map(RobotDto::from);
  }

  /** Returns all upgrades available in the catalog as DTOs. */
  public List<UpgradeDto> findAllUpgrades() {
    return upgradeRepository.findAll().stream().map(UpgradeDto::from).toList();
  }
}
