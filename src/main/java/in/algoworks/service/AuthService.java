
package in.algoworks.service;

import in.algoworks.dto.AuthResponseDTO;
import in.algoworks.dto.LoginRequestDTO;
import in.algoworks.dto.SignupRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.repository.CustomerRepository;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Register Customer
    public AuthResponseDTO signup(SignupRequestDTO request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!customerRepository
                .findAllByEmailIgnoreCase(email)
                .isEmpty()) {
            throw emailAlreadyRegistered();
        }

        // Create customer and hash password
        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(email);
        customer.setRole("CUSTOMER");
        customer.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        // Save customer to MongoDB
        Customer savedCustomer;

        try {
            savedCustomer = customerRepository.save(customer);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }

        // Generate JWT after successful signup
        String token = jwtService.generateToken(
                savedCustomer.getId(),
                savedCustomer.getEmail(),
                "CUSTOMER"
        );

        return new AuthResponseDTO(
                savedCustomer.getId(),
                savedCustomer.getName(),
                savedCustomer.getEmail(),
                token
        );
    }

    // Login Customer
    public AuthResponseDTO login(LoginRequestDTO request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        // Fail closed if legacy records contain ambiguous case-insensitive emails.
        var customers = customerRepository.findAllByEmailIgnoreCase(email);
        if (customers.size() != 1) {
            throw invalidCredentials();
        }
        Customer customer = customers.getFirst();

        // Verify password against stored password hash
        if (customer.getPasswordHash() == null
                || !passwordEncoder.matches(
                request.getPassword(),
                customer.getPasswordHash())) {

            throw invalidCredentials();
        }

        // Generate JWT after successful login
        String token = jwtService.generateToken(
                customer.getId(),
                customer.getEmail(),
                "ADMIN".equalsIgnoreCase(customer.getRole())
                        ? "ADMIN"
                        : "CUSTOMER"
        );

        return new AuthResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                token
        );
    }

    // Duplicate email response
    private ResponseStatusException emailAlreadyRegistered() {
        return new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Email already registered"
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }
}
