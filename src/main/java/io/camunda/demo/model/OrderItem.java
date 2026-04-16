package io.camunda.demo.model;

import jakarta.persistence.Column;
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

  /**
   * Reference to the robot product ordered (model ID from the product catalog,
   * e.g. "WALL-E" or "C3PO").
   */
  @Column(nullable = false)
  private String productReference;

  @Column(nullable = false)
  private int quantity;

  protected OrderItem() {
  }

  public OrderItem(Order order, String productReference, int quantity) {
    this.order = order;
    this.productReference = productReference;
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

  public String getProductReference() {
    return productReference;
  }

  public void setProductReference(String productReference) {
    this.productReference = productReference;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @Override
  public String toString() {
    return "OrderItem{id=" + id + ", productReference='" + productReference
        + "', quantity=" + quantity + "}";
  }
}
