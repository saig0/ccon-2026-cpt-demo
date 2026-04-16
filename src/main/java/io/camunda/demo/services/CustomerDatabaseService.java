package io.camunda.demo.services;

import io.camunda.demo.model.Customer;
import io.camunda.demo.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading and querying the Camunda Robotics customer database.
 */
@Service
@Transactional(readOnly = true)
public class CustomerDatabaseService {

  private final CustomerRepository customerRepository;

  public CustomerDatabaseService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /** Returns all customers. */
  public List<Customer> findAllCustomers() {
    return customerRepository.findAll();
  }

  /** Returns a single customer by their unique ID. */
  public Optional<Customer> findCustomerById(Long id) {
    return customerRepository.findById(id);
  }

  /** Returns a single customer by their name. */
  public Optional<Customer> findCustomerByName(String name) {
    return customerRepository.findByName(name);
  }
}
