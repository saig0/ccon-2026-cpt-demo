package io.camunda.demo.catalog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a robot product in the Camunda Robotics catalog.
 * Multiple robots can share the same {@code modelId} but differ by {@code modelVersion}.
 */
@Entity
@Table(name = "robots")
public class Robot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** Model identifier shared across versions, e.g. "C3PO" or "R2D2". */
  @Column(nullable = false)
  private String modelId;

  /** Version of this model, e.g. "1.0" or "2.0". */
  @Column(nullable = false)
  private String modelVersion;

  @Column(nullable = false)
  private String name;

  @Column(length = 1000)
  private String description;

  /** Core intent/category, e.g. {@link RobotIntent#TRANSLATION} or {@link RobotIntent#GUARD}. */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RobotIntent intent;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  /**
   * Upgrades that are compatible with this robot model/version.
   * EAGER loading is intentional: the catalog dataset is small and the worker always
   * needs the full upgrade list when serialising robots to process variables.
   */
  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "robot_compatible_upgrades",
      joinColumns = @JoinColumn(name = "robot_id"),
      inverseJoinColumns = @JoinColumn(name = "upgrade_id")
  )
  private List<Upgrade> compatibleUpgrades = new ArrayList<>();

  protected Robot() {
  }

  public Robot(String modelId, String modelVersion, String name, String description, RobotIntent intent,
      BigDecimal price) {
    this.modelId = modelId;
    this.modelVersion = modelVersion;
    this.name = name;
    this.description = description;
    this.intent = intent;
    this.price = price;
  }

  public Long getId() {
    return id;
  }

  public String getModelId() {
    return modelId;
  }

  public void setModelId(String modelId) {
    this.modelId = modelId;
  }

  public String getModelVersion() {
    return modelVersion;
  }

  public void setModelVersion(String modelVersion) {
    this.modelVersion = modelVersion;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public RobotIntent getIntent() {
    return intent;
  }

  public void setIntent(RobotIntent intent) {
    this.intent = intent;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public void setPrice(BigDecimal price) {
    this.price = price;
  }

  public List<Upgrade> getCompatibleUpgrades() {
    return compatibleUpgrades;
  }

  public void setCompatibleUpgrades(List<Upgrade> compatibleUpgrades) {
    this.compatibleUpgrades = compatibleUpgrades;
  }

  @Override
  public String toString() {
    return "Robot{id=" + id + ", modelId='" + modelId + "', modelVersion='" + modelVersion
        + "', name='" + name + "', intent='" + intent + "', price=" + price + "}";
  }
}
