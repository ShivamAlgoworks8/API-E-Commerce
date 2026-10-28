package in.algoworks.controller;

import in.algoworks.dto.AuthResponseDTO;
import in.algoworks.dto.LoginRequestDTO;
import in.algoworks.dto.SignupRequestDTO;
import in.algoworks.service.AuthService;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Customer Signup (Consumer Portal)
    @PostMapping("/customer/signup")
    public AuthResponseDTO customerSignup(
            @Valid @RequestBody SignupRequestDTO request) {
        return authService.signupCustomer(request);
    }

    // Customer Login (Consumer Portal)
    @PostMapping("/customer/login")
    public AuthResponseDTO customerLogin(
            @Valid @RequestBody LoginRequestDTO request) {
        return authService.loginCustomer(request);
    }

    // Admin Signup (Nexora Admin Portal)
    @PostMapping("/admin/signup")
    public AuthResponseDTO adminSignup(
            @Valid @RequestBody SignupRequestDTO request) {
        return authService.signupAdmin(request);
    }

    // Admin Login (Nexora Admin Portal)
    @PostMapping("/admin/login")
    public AuthResponseDTO adminLogin(
            @Valid @RequestBody LoginRequestDTO request) {
        return authService.loginAdmin(request);
    }

    // Backwards-compatible general customer signup
    @PostMapping("/signup")
    public AuthResponseDTO signup(
            @Valid @RequestBody SignupRequestDTO request) {
        return authService.signup(request);
    }

    // Backwards-compatible general customer login
    @PostMapping("/login")
    public AuthResponseDTO login(
            @Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
