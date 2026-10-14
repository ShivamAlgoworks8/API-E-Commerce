
package in.algoworks.service;

import in.algoworks.entity.Customer;
import in.algoworks.repository.CustomerRepository;
import in.algoworks.dto.CustomerRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer createCustomer(CustomerRequestDTO customerReq) {
        Customer customer = new Customer();
        customer.setName(customerReq.getName());
        customer.setEmail(customerReq.getEmail());
        customer.setPhone(customerReq.getPhone());
        customer.setAddress(customerReq.getAddress());
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

    public Customer updateCustomer(String id, CustomerRequestDTO customerReq) {
        Customer customer = getCustomer(id);

        customer.setName(customerReq.getName());
        customer.setEmail(customerReq.getEmail());
        customer.setPhone(customerReq.getPhone());
        customer.setAddress(customerReq.getAddress());

        return customerRepository.save(customer);
    }

    public boolean deleteCustomer(String id) {
        Customer customer = getCustomer(id);
        customerRepository.delete(customer);
        return true;
    }
}
