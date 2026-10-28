package in.algoworks.service;

import in.algoworks.dto.AuthResponseDTO;
import in.algoworks.dto.LoginRequestDTO;
import in.algoworks.dto.SignupRequestDTO;
import in.algoworks.entity.Admin;
import in.algoworks.entity.Customer;
import in.algoworks.repository.AdminRepository;
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
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            CustomerRepository customerRepository,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.customerRepository = customerRepository;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Default Customer Signup
    public AuthResponseDTO signup(SignupRequestDTO request) {
        return signupCustomer(request);
    }

    // Default Customer Login
    public AuthResponseDTO login(LoginRequestDTO request) {
        return loginCustomer(request);
    }

    // Register Customer (Consumer portal) -> saved into 'customers' collection
    public AuthResponseDTO signupCustomer(SignupRequestDTO request) {
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!customerRepository.findAllByEmailIgnoreCase(email).isEmpty()) {
            throw emailAlreadyRegistered();
        }

        Customer customer = new Customer();
        customer.setName(request.getName().trim());
        customer.setEmail(email);
        customer.setRole("CUSTOMER");
        customer.setStatus("Active");
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Customer savedCustomer;
        try {
            savedCustomer = customerRepository.save(customer);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }

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

    // Login Customer (Consumer portal) -> validates against 'customers' collection
    public AuthResponseDTO loginCustomer(LoginRequestDTO request) {
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        var customers = customerRepository.findAllByEmailIgnoreCase(email);
        if (customers.size() != 1) {
            throw invalidCredentials();
        }
        Customer customer = customers.getFirst();

        if (customer.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(
                customer.getId(),
                customer.getEmail(),
                "CUSTOMER"
        );

        return new AuthResponseDTO(
                customer.getId(),
                customer.getName(),
                customer.getEmail(),
                token
        );
    }

    // Register Admin (Admin portal) -> saved into 'admins' collection
    public AuthResponseDTO signupAdmin(SignupRequestDTO request) {
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (!adminRepository.findAllByEmailIgnoreCase(email).isEmpty()) {
            throw emailAlreadyRegistered();
        }

        Admin admin = new Admin();
        admin.setName(request.getName().trim());
        admin.setEmail(email);
        admin.setRole("ADMIN");
        admin.setStatus("Active");
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Admin savedAdmin;
        try {
            savedAdmin = adminRepository.save(admin);
        } catch (DuplicateKeyException exception) {
            throw emailAlreadyRegistered();
        }

        String token = jwtService.generateToken(
                savedAdmin.getId(),
                savedAdmin.getEmail(),
                "ADMIN"
        );

        return new AuthResponseDTO(
                savedAdmin.getId(),
                savedAdmin.getName(),
                savedAdmin.getEmail(),
                token
        );
    }

    // Login Admin (Admin portal) -> validates against 'admins' collection
    public AuthResponseDTO loginAdmin(LoginRequestDTO request) {
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        var admins = adminRepository.findAllByEmailIgnoreCase(email);
        if (admins.size() != 1) {
            throw invalidCredentials();
        }
        Admin admin = admins.getFirst();

        if (admin.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw invalidCredentials();
        }

        String token = jwtService.generateToken(
                admin.getId(),
                admin.getEmail(),
                "ADMIN"
        );

        return new AuthResponseDTO(
                admin.getId(),
                admin.getName(),
                admin.getEmail(),
                token
        );
    }

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
