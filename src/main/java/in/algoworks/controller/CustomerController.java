
package in.algoworks.controller;
import in.algoworks.dto.CustomerRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "http://localhost:3001")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    public Customer createCustomer(
            @RequestBody CustomerRequestDTO customerReq) {
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
            @RequestBody CustomerRequestDTO customerReq) {
        return customerService.updateCustomer(id, customerReq);
    }

    @DeleteMapping("/{id}")
    public boolean deleteCustomer(@PathVariable String id) {
        return customerService.deleteCustomer(id);
    }
}
