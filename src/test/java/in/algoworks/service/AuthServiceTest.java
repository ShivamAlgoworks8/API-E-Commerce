package in.algoworks.service;

import in.algoworks.dto.AuthResponseDTO;
import in.algoworks.dto.LoginRequestDTO;
import in.algoworks.dto.SignupRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private CustomerRepository customerRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        customerRepository = mock(CustomerRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authService = new AuthService(
                customerRepository, passwordEncoder, jwtService);
    }

    @Test
    void signupReturnsTokenAndStoresNormalizedEmailWithHashedPassword() {
        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of());
        when(passwordEncoder.encode("long-password")).thenReturn("hashed-password");
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer customer = invocation.getArgument(0);
            customer.setId("customer-id");
            return customer;
        });
        when(jwtService.generateToken("customer-id", "person@example.com", "CUSTOMER"))
                .thenReturn("signed-token");

        SignupRequestDTO request = new SignupRequestDTO();
        request.setName(" Person ");
        request.setEmail(" Person@Example.com ");
        request.setPassword("long-password");

        AuthResponseDTO response = authService.signup(request);

        assertEquals("customer-id", response.getId());
        assertEquals("Person", response.getName());
        assertEquals("person@example.com", response.getEmail());
        assertEquals("signed-token", response.getToken());
        verify(passwordEncoder).encode("long-password");
    }

    @Test
    void duplicateEmailReturnsConflictWithoutSavingAnotherCustomer() {
        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of(new Customer()));
        SignupRequestDTO request = new SignupRequestDTO();
        request.setName("Person");
        request.setEmail("PERSON@example.com");
        request.setPassword("long-password");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.signup(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    void concurrentDuplicateEmailInsertReturnsConflict() {
        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of());
        when(passwordEncoder.encode("long-password")).thenReturn("hashed-password");
        when(customerRepository.save(any(Customer.class)))
                .thenThrow(new DuplicateKeyException("duplicate email"));

        SignupRequestDTO request = new SignupRequestDTO();
        request.setName("Person");
        request.setEmail("person@example.com");
        request.setPassword("long-password");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.signup(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    void loginMatchesEmailWithoutCaseSensitivityAndReturnsToken() {
        Customer customer = new Customer();
        customer.setId("customer-id");
        customer.setName("Person");
        customer.setEmail("person@example.com");
        customer.setPasswordHash("hashed-password");
        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of(customer));
        when(passwordEncoder.matches("long-password", "hashed-password"))
                .thenReturn(true);
        when(jwtService.generateToken("customer-id", "person@example.com", "CUSTOMER"))
                .thenReturn("signed-token");

        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail(" PERSON@EXAMPLE.COM ");
        request.setPassword("long-password");

        AuthResponseDTO response = authService.login(request);

        assertEquals("signed-token", response.getToken());
        assertEquals("person@example.com", response.getEmail());
    }

    @Test
    void loginRejectsAmbiguousLegacyEmailAndBadPassword() {
        Customer first = new Customer();
        first.setEmail("person@example.com");
        Customer second = new Customer();
        second.setEmail("PERSON@example.com");
        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of(first, second));
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("person@example.com");
        request.setPassword("long-password");

        ResponseStatusException ambiguous = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, ambiguous.getStatusCode());

        when(customerRepository.findAllByEmailIgnoreCase("person@example.com"))
                .thenReturn(List.of(first));
        first.setPasswordHash("hashed-password");
        when(passwordEncoder.matches("long-password", "hashed-password"))
                .thenReturn(false);

        ResponseStatusException badPassword = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(request));
        assertEquals(HttpStatus.UNAUTHORIZED, badPassword.getStatusCode());
    }
}
