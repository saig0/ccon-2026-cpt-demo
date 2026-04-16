package io.camunda.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer order at Camunda Robotics.
 */
@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", nullable = false)
  private Customer customer;

  @Column(nullable = false)
  private LocalDate orderDate;

  @Column(nullable = false)
  private String shipmentAddressStreet;

  @Column(nullable = false)
  private String shipmentAddressCity;

  @Column(nullable = false)
  private String shipmentAddressCountry;

  @Column(nullable = false)
  private LocalDate shipmentDate;

  @Column(nullable = false)
  private LocalDate paymentDate;

  /** Total price of the order in €. */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal paymentAmount;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<OrderItem> items = new ArrayList<>();

  protected Order() {
  }

  public Order(Customer customer, LocalDate orderDate,
      String shipmentAddressStreet, String shipmentAddressCity, String shipmentAddressCountry,
      LocalDate shipmentDate, LocalDate paymentDate, BigDecimal paymentAmount) {
    this.customer = customer;
    this.orderDate = orderDate;
    this.shipmentAddressStreet = shipmentAddressStreet;
    this.shipmentAddressCity = shipmentAddressCity;
    this.shipmentAddressCountry = shipmentAddressCountry;
    this.shipmentDate = shipmentDate;
    this.paymentDate = paymentDate;
    this.paymentAmount = paymentAmount;
  }

  public Long getId() {
    return id;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public String getShipmentAddressStreet() {
    return shipmentAddressStreet;
  }

  public void setShipmentAddressStreet(String shipmentAddressStreet) {
    this.shipmentAddressStreet = shipmentAddressStreet;
  }

  public String getShipmentAddressCity() {
    return shipmentAddressCity;
  }

  public void setShipmentAddressCity(String shipmentAddressCity) {
    this.shipmentAddressCity = shipmentAddressCity;
  }

  public String getShipmentAddressCountry() {
    return shipmentAddressCountry;
  }

  public void setShipmentAddressCountry(String shipmentAddressCountry) {
    this.shipmentAddressCountry = shipmentAddressCountry;
  }

  public LocalDate getShipmentDate() {
    return shipmentDate;
  }

  public void setShipmentDate(LocalDate shipmentDate) {
    this.shipmentDate = shipmentDate;
  }

  public LocalDate getPaymentDate() {
    return paymentDate;
  }

  public void setPaymentDate(LocalDate paymentDate) {
    this.paymentDate = paymentDate;
  }

  public BigDecimal getPaymentAmount() {
    return paymentAmount;
  }

  public void setPaymentAmount(BigDecimal paymentAmount) {
    this.paymentAmount = paymentAmount;
  }

  public List<OrderItem> getItems() {
    return items;
  }

  public void setItems(List<OrderItem> items) {
    this.items = items;
  }

  @Override
  public String toString() {
    return "Order{id=" + id + ", orderDate=" + orderDate + ", paymentAmount=" + paymentAmount + "}";
  }
}
