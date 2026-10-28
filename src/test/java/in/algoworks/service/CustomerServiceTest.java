package in.algoworks.service;

import in.algoworks.dto.CustomerRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerServiceTest {

    private final CustomerRepository customerRepository =
            mock(CustomerRepository.class);
    private final CustomerService customerService =
            new CustomerService(customerRepository);

    @Test
    void customerCreationRejectsCaseInsensitiveDuplicateEmail() {
        when(customerRepository.findAllByEmailIgnoreCase("PERSON@example.com"))
                .thenReturn(List.of(new Customer()));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.createCustomer(request("PERSON@example.com")));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void customerUpdateAllowsKeepingOwnEmailButRejectsAnotherCustomersEmail() {
        Customer existing = new Customer();
        existing.setId("current-customer");
        existing.setEmail("person@example.com");
        Customer other = new Customer();
        other.setId("other-customer");
        other.setEmail("other@example.com");
        when(customerRepository.findById("current-customer"))
                .thenReturn(Optional.of(existing));
        when(customerRepository.findAllByEmailIgnoreCase("PERSON@example.com"))
                .thenReturn(List.of(existing));
        when(customerRepository.findAllByEmailIgnoreCase("OTHER@example.com"))
                .thenReturn(List.of(other));
        when(customerRepository.save(any(Customer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Customer updated = customerService.updateCustomer(
                "current-customer", request("PERSON@example.com"));
        assertEquals("PERSON@example.com", updated.getEmail());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> customerService.updateCustomer(
                        "current-customer", request("OTHER@example.com")));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    private static CustomerRequestDTO request(String email) {
        CustomerRequestDTO request = new CustomerRequestDTO();
        request.setName("Person");
        request.setEmail(email);
        return request;
    }
}
