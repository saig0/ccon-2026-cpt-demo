package io.camunda.demo.repositories;

import io.camunda.demo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Order} entities.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

}
