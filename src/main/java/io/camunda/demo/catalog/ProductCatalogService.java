package io.camunda.demo.catalog;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading and querying the Camunda Robotics product catalog.
 */
@Service
@Transactional(readOnly = true)
public class ProductCatalogService {

  private final RobotRepository robotRepository;
  private final UpgradeRepository upgradeRepository;

  public ProductCatalogService(RobotRepository robotRepository,
      UpgradeRepository upgradeRepository) {
    this.robotRepository = robotRepository;
    this.upgradeRepository = upgradeRepository;
  }

  /** Returns all robots in the catalog. */
  public List<Robot> findAllRobots() {
    return robotRepository.findAll();
  }

  /** Returns all robots that match a given intent/category. */
  public List<Robot> findRobotsByIntent(RobotIntent intent) {
    return robotRepository.findByIntent(intent);
  }

  /** Returns all versions of a robot model. */
  public List<Robot> findRobotsByModelId(String modelId) {
    return robotRepository.findByModelId(modelId);
  }

  /** Returns a single robot by its unique ID. */
  public Optional<Robot> findRobotById(Long id) {
    return robotRepository.findById(id);
  }

  /** Returns all upgrades available in the catalog. */
  public List<Upgrade> findAllUpgrades() {
    return upgradeRepository.findAll();
  }
}
