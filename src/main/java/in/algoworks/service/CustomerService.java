
package in.algoworks.service;

import in.algoworks.entity.Customer;
import in.algoworks.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomer(String id) {
        return customerRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Customer not found: " + id));
    }

    public Customer updateCustomer(String id, Customer request) {
        Customer customer = getCustomer(id);

        customer.setName(request.getName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());

        return customerRepository.save(customer);
    }

    public boolean deleteCustomer(String id) {
        Customer customer = getCustomer(id);
        customerRepository.delete(customer);
        return true;
    }
}
