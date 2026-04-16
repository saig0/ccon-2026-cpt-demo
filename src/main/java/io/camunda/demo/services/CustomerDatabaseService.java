package io.camunda.demo.services;

import io.camunda.demo.dto.CustomerDto;
import io.camunda.demo.repositories.CustomerRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for loading and querying the Camunda Robotics customer database.
 * Returns {@link CustomerDto} objects instead of JPA entities to avoid lazy-loading issues
 * outside the transaction boundary.
 */
@Service
@Transactional(readOnly = true)
public class CustomerDatabaseService {

  private final CustomerRepository customerRepository;

  public CustomerDatabaseService(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  /** Returns all customers as DTOs. */
  public List<CustomerDto> findAllCustomers() {
    return customerRepository.findAll().stream().map(CustomerDto::from).toList();
  }

  /** Returns a single customer by their unique ID as a DTO. */
  public Optional<CustomerDto> findCustomerById(Long id) {
    return customerRepository.findById(id).map(CustomerDto::from);
  }

  /** Returns a single customer by their name as a DTO. */
  public Optional<CustomerDto> findCustomerByName(String name) {
    return customerRepository.findByName(name).map(CustomerDto::from);
  }
}
