
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

    // Register Customer
    @PostMapping("/signup")
    public AuthResponseDTO signup(
            @Valid @RequestBody SignupRequestDTO request) {
        return authService.signup(request);
    }

    // Login Customer
    @PostMapping("/login")
    public AuthResponseDTO login(
            @Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}
