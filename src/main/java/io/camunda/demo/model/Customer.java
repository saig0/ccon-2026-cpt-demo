package io.camunda.demo.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a customer of Camunda Robotics.
 */
@Entity
@Table(name = "customers")
public class Customer {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String email;

  @Column(nullable = false)
  private String addressStreet;

  @Column(nullable = false)
  private String addressCity;

  @Column(nullable = false)
  private String addressCountry;

  /** Payment method, e.g. "CREDIT_CARD" or "PAYPAL". */
  @Column(nullable = false)
  private String paymentMethod;

  /** Masked or partial payment reference, e.g. "**** **** **** 4242" or "user@example.com". */
  @Column(nullable = false)
  private String paymentReference;

  /** Whether the customer is allowed to purchase robots. */
  @Column(nullable = false)
  private boolean canBuyRobots;

  /**
   * Whether the customer is allowed to purchase security robots (intent: GUARD).
   * Requires an additional compliance check.
   */
  @Column(nullable = false)
  private boolean canBuySecurityRobots;

  @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<Order> orders = new ArrayList<>();

  protected Customer() {
  }

  public Customer(String name, String email,
      String addressStreet, String addressCity, String addressCountry,
      String paymentMethod, String paymentReference,
      boolean canBuyRobots, boolean canBuySecurityRobots) {
    this.name = name;
    this.email = email;
    this.addressStreet = addressStreet;
    this.addressCity = addressCity;
    this.addressCountry = addressCountry;
    this.paymentMethod = paymentMethod;
    this.paymentReference = paymentReference;
    this.canBuyRobots = canBuyRobots;
    this.canBuySecurityRobots = canBuySecurityRobots;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getAddressStreet() {
    return addressStreet;
  }

  public void setAddressStreet(String addressStreet) {
    this.addressStreet = addressStreet;
  }

  public String getAddressCity() {
    return addressCity;
  }

  public void setAddressCity(String addressCity) {
    this.addressCity = addressCity;
  }

  public String getAddressCountry() {
    return addressCountry;
  }

  public void setAddressCountry(String addressCountry) {
    this.addressCountry = addressCountry;
  }

  public String getPaymentMethod() {
    return paymentMethod;
  }

  public void setPaymentMethod(String paymentMethod) {
    this.paymentMethod = paymentMethod;
  }

  public String getPaymentReference() {
    return paymentReference;
  }

  public void setPaymentReference(String paymentReference) {
    this.paymentReference = paymentReference;
  }

  public boolean isCanBuyRobots() {
    return canBuyRobots;
  }

  public void setCanBuyRobots(boolean canBuyRobots) {
    this.canBuyRobots = canBuyRobots;
  }

  public boolean isCanBuySecurityRobots() {
    return canBuySecurityRobots;
  }

  public void setCanBuySecurityRobots(boolean canBuySecurityRobots) {
    this.canBuySecurityRobots = canBuySecurityRobots;
  }

  public List<Order> getOrders() {
    return orders;
  }

  public void setOrders(List<Order> orders) {
    this.orders = orders;
  }

  @Override
  public String toString() {
    return "Customer{id=" + id + ", name='" + name + "', email='" + email
        + "', city='" + addressCity + "'}";
  }
}
