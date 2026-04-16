package io.camunda.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a single line item within a customer {@link Order}.
 * Exactly one of {@link #robot} or {@link #upgrade} is non-null.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private Order order;

  /** The robot that was ordered. Null if this item is an upgrade. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "robot_id")
  private Robot robot;

  /** The upgrade that was ordered. Null if this item is a robot. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "upgrade_id")
  private Upgrade upgrade;

  private int quantity;

  protected OrderItem() {
  }

  /** Creates an order item for a robot. */
  public OrderItem(Order order, Robot robot, int quantity) {
    this.order = order;
    this.robot = robot;
    this.quantity = quantity;
  }

  /** Creates an order item for an upgrade. */
  public OrderItem(Order order, Upgrade upgrade, int quantity) {
    this.order = order;
    this.upgrade = upgrade;
    this.quantity = quantity;
  }

  public Long getId() {
    return id;
  }

  public Order getOrder() {
    return order;
  }

  public void setOrder(Order order) {
    this.order = order;
  }

  public Robot getRobot() {
    return robot;
  }

  public void setRobot(Robot robot) {
    this.robot = robot;
  }

  public Upgrade getUpgrade() {
    return upgrade;
  }

  public void setUpgrade(Upgrade upgrade) {
    this.upgrade = upgrade;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    final String product = robot != null ? "robot=" + robot.getModelId()
        : (upgrade != null ? "upgrade=" + upgrade.getName() : "none");
    return "OrderItem{id=" + id + ", " + product + ", quantity=" + quantity + "}";
  }
}
