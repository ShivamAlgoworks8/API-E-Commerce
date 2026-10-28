package in.algoworks.controller;

import in.algoworks.dto.CustomerRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.service.CustomerService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer createCustomer(
            @Valid @RequestBody CustomerRequestDTO customerReq) {
        return customerService.createCustomer(customerReq);
    }

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable String id) {
        return customerService.getCustomer(id);
    }

    @PutMapping("/{id}")
    public Customer updateCustomer(
            @PathVariable String id,
            @Valid @RequestBody CustomerRequestDTO customerReq) {
        return customerService.updateCustomer(id, customerReq);
    }

    // NEW: Update customer Active/Inactive status
    @PatchMapping("/{id}/status")
    public Customer updateCustomerStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");

        return customerService.updateCustomerStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public boolean deleteCustomer(@PathVariable String id) {
        return customerService.deleteCustomer(id);
    }
}
