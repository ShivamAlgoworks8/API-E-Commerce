
package in.algoworks.service;

import in.algoworks.dto.CustomerRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.CustomerRepository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // Create customer
    public Customer createCustomer(CustomerRequestDTO customerReq) {
        String email = customerReq.getEmail();
        if (!customerRepository.findAllByEmailIgnoreCase(email).isEmpty()) {
            throw emailAlreadyRegistered();
        }

        Customer customer = new Customer();
        customer.setName(customerReq.getName());
        customer.setEmail(email);
        customer.setPhone(customerReq.getPhone());
        customer.setAddress(customerReq.getAddress());

        // Customer status defaults to Active in Customer.java
        try {
            return customerRepository.save(customer);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }
    }

    // Get all customers
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    // Get customer by ID
    public Customer getCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Customer not found: " + id));
    }

    // Update customer
    public Customer updateCustomer(
            String id, CustomerRequestDTO customerReq) {

        Customer customer = getCustomer(id);

        // Check email only if it belongs to another customer
        String email = customerReq.getEmail();
        customerRepository.findAllByEmailIgnoreCase(email)
                .stream()
                .filter(existing -> !Objects.equals(existing.getId(), id))
                .findAny()
                .ifPresent(existing -> {
                    throw emailAlreadyRegistered();
                });

        customer.setName(customerReq.getName());
        customer.setEmail(email);
        customer.setPhone(customerReq.getPhone());
        customer.setAddress(customerReq.getAddress());

        // Existing status remains unchanged during profile update
        try {
            return customerRepository.save(customer);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }
    }
    // Update customer status (Admin action)
    public Customer updateCustomerStatus(String id, String status) {

        Customer customer = getCustomer(id);

        status = status == null ? null : status.trim();
        if (!"Active".equalsIgnoreCase(status)
                && !"Inactive".equalsIgnoreCase(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be Active or Inactive");
        }

        customer.setStatus(
                status.substring(0, 1).toUpperCase()
                        + status.substring(1).toLowerCase()
        );

        return customerRepository.save(customer);
    }

    // Delete customer
    public boolean deleteCustomer(String id) {
        Customer customer = getCustomer(id);
        customerRepository.delete(customer);

        return true;
    }

    private ResponseStatusException emailAlreadyRegistered() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT, "Email already registered");
    }
}
