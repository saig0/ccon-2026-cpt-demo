package io.camunda.demo.model;

/**
 * Lifecycle status of a customer order at Camunda Robotics.
 */
public enum OrderStatus {

  /** Order has been created and is awaiting payment. */
  CREATED,

  /** Payment has been charged successfully. */
  PAID,

  /** Order has been handed to the logistics team; an estimated delivery date has been set. */
  PREPARED_FOR_SHIPPING,

  /** Order is on its way to the customer. */
  ON_DELIVERY,

  /** Order has been delivered to the customer. */
  DELIVERED
}
