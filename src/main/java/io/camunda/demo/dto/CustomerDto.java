package io.camunda.demo.dto;

import io.camunda.demo.model.Customer;
import java.util.List;

/** Type-safe DTO for a customer in the Camunda Robotics customer database. */
public record CustomerDto(
    Long id,
    String name,
    String email,
    AddressDto address,
    PaymentInfoDto paymentInfo,
    List<OrderDto> orders) {

  public static CustomerDto from(Customer customer) {
    return new CustomerDto(
        customer.getId(),
        customer.getName(),
        customer.getEmail(),
        new AddressDto(
            customer.getAddressStreet(), customer.getAddressCity(), customer.getAddressCountry()),
        new PaymentInfoDto(customer.getPaymentMethod(), customer.getPaymentReference()),
        customer.getOrders().stream().map(OrderDto::from).toList());
  }
}
