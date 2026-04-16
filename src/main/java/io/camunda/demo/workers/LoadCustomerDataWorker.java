package io.camunda.demo.workers;

import io.camunda.client.annotation.JobWorker;
import io.camunda.client.annotation.Variable;
import io.camunda.client.api.response.ActivatedJob;
import io.camunda.demo.services.CustomerDatabaseService;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Job worker that loads customer data from the Camunda Robotics customer database.
 *
 * <p>If a {@code customerName} variable is present on the job, only the matching customer is
 * returned (as {@code customer}). Otherwise all customers are returned (as {@code customers}).
 */
@Component
public class LoadCustomerDataWorker {

  private static final Logger LOG = LoggerFactory.getLogger(LoadCustomerDataWorker.class);

  private final CustomerDatabaseService customerDatabaseService;

  public LoadCustomerDataWorker(CustomerDatabaseService customerDatabaseService) {
    this.customerDatabaseService = customerDatabaseService;
  }

  @JobWorker(type = "load-customer-data")
  public Map<String, Object> loadCustomerData(
      final ActivatedJob job,
      @Variable(name = "customerName") @Nullable String customerName) {

    LOG.info("Processing load-customer-data job: {}", job.getKey());

    if (customerName != null && !customerName.isBlank()) {
      return customerDatabaseService.findCustomerByName(customerName)
          .map(customer -> {
            final CustomerDto customerDto = CustomerDto.from(customer);
            LOG.info("Loaded customer '{}' with {} order(s)", customerDto.name(), customerDto.orders().size());
            return Map.<String, Object>of("customer", customerDto);
          })
          .orElseGet(() -> {
            LOG.warn("No customer found with name '{}'", customerName);
            return Map.of();
          });
    }

    final List<CustomerDto> customers = customerDatabaseService.findAllCustomers().stream()
        .map(CustomerDto::from)
        .toList();
    LOG.info("Loaded {} customer(s) from customer database", customers.size());

    LOG.info("load-customer-data completed: {}", job.getKey());
    return Map.of("customers", customers, "customerCount", customers.size());
  }
}
