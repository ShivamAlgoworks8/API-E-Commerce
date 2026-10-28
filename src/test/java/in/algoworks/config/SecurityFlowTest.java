package in.algoworks.config;

import in.algoworks.dto.AuthResponseDTO;
import in.algoworks.entity.Merchant;
import in.algoworks.entity.Order;
import in.algoworks.entity.Payment;
import in.algoworks.entity.Product;
import in.algoworks.service.AuthService;
import in.algoworks.service.JwtService;
import in.algoworks.service.MerchantService;
import in.algoworks.service.OrderService;
import in.algoworks.service.PaymentService;
import in.algoworks.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://127.0.0.1:27017/ecommerce_test"
})
@AutoConfigureMockMvc
class SecurityFlowTest {

    private static final String TEST_JWT_SECRET = createTestSecret();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private MerchantService merchantService;

    @MockitoBean
    private PaymentService paymentService;

    @DynamicPropertySource
    static void registerJwtSecret(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> TEST_JWT_SECRET);
    }

    @Test
    void signupAndPreflightWorkForConfiguredFrontendOrigin() throws Exception {
        when(authService.signup(any())).thenReturn(
                new AuthResponseDTO("customer-id", "Person",
                        "person@example.com", "signed-token"));

        mockMvc.perform(post("/api/auth/signup")
                        .header("Origin", "http://localhost:3001")
                        .contentType("application/json")
                        .content("""
                                {"name":"Person","email":"person@example.com","password":"long-password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "http://localhost:3001"));

        mockMvc.perform(options("/api/auth/signup")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "http://localhost:5173"));

        mockMvc.perform(options("/api/auth/signup")
                        .header("Origin", "http://localhost:3002")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin", "http://localhost:3002"));
    }

    @Test
    void loginRouteIsPublicAndDevelopmentUserIsNotAutoConfigured()
            throws Exception {
        when(authService.login(any())).thenReturn(
                new AuthResponseDTO("customer-id", "Person",
                        "person@example.com", "signed-token"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"person@example.com","password":"long-password"}
                                """))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertTrue(
                applicationContext.getBeansOfType(UserDetailsService.class).isEmpty());
    }

    @Test
    void authErrorsKeepTheirIntendedHttpStatus() throws Exception {
        when(authService.signup(any())).thenThrow(
                new ResponseStatusException(
                        HttpStatus.CONFLICT, "Email already registered"));
        when(authService.login(any())).thenThrow(
                new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType("application/json")
                        .content("""
                                {"name":"Person","email":"person@example.com","password":"long-password"}
                                """))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("""
                                {"email":"person@example.com","password":"wrong-password"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void signupCorsRejectsOriginsOutsideConfiguredAllowlist() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .header("Origin", "https://unconfigured.example")
                        .contentType("application/json")
                        .content("""
                                {"name":"Person","email":"person@example.com","password":"long-password"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void ordersAreAccessibleAndRejectInvalidBearerTokens() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        String token = jwtService.generateToken(
                "customer-id", "person@example.com");

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedListEndpointsResolveForProductsMerchantsOrdersAndPayments()
            throws Exception {
        when(productService.getAllProduct()).thenReturn(List.of(new Product()));
        when(merchantService.getAllMerchant()).thenReturn(List.of(new Merchant()));
        when(orderService.getAllOrders()).thenReturn(List.of(new Order()));
        when(paymentService.getAllPayments()).thenReturn(List.of(new Payment()));
        String token = jwtService.generateToken(
                "customer-id", "person@example.com");

        for (String path : List.of(
                "/api/products", "/api/merchants", "/api/orders", "/api/payments")) {
            mockMvc.perform(get(path)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void productEndpointsAreAccessibleAndRejectInvalidBearerTokens()
            throws Exception {
        when(productService.getAllProduct()).thenReturn(List.of(new Product()));
        when(productService.getProduct("prod-1")).thenReturn(new Product());
        when(productService.createProduct(any())).thenReturn(new Product());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products/prod-1"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("""
                                {"name":"Laptop","price":999.0,"productType":"Electronics","status":"Available"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    private static String createTestSecret() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
