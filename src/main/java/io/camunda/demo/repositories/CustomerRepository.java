package io.camunda.demo.repositories;

import io.camunda.demo.model.Customer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Customer} entities.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

  /** Find a customer by name (case-sensitive). */
  Optional<Customer> findByName(String name);
}
