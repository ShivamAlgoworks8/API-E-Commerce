package in.algoworks.config;

import in.algoworks.entity.Order;
import in.algoworks.service.AuthService;
import in.algoworks.service.JwtService;
import in.algoworks.service.MerchantService;
import in.algoworks.service.OrderService;
import in.algoworks.service.PaymentService;
import in.algoworks.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.data.mongodb.uri=mongodb://127.0.0.1:27017/ecommerce_test"
})
@AutoConfigureMockMvc
class OrderTrackingSecurityTest {

    private static final String TEST_JWT_SECRET = createTestSecret();

    @Autowired
    private MockMvc mockMvc;

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

    private String bearer(String id, String role) {
        return "Bearer " + jwtService.generateToken(id, id + "@example.com", role);
    }

    @Test
    void allOrdersAreAdminOnly() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer("c1", "CUSTOMER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", bearer("a1", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void myOrdersRequireACustomerToken() throws Exception {
        when(orderService.getCustomerOrders("c1")).thenReturn(List.of(new Order()));

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization", bearer("a1", "ADMIN")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/orders/my-orders")
                        .header("Authorization", bearer("c1", "CUSTOMER")))
                .andExpect(status().isOk());
    }

    @Test
    void orderDetailsNeedAuthenticationAndPassCallerIdentity() throws Exception {
        Order order = new Order();
        order.setId("o1");
        when(orderService.getOrderForUser("o1", "c1", false)).thenReturn(order);
        when(orderService.getOrderForUser("o1", "a1", true)).thenReturn(order);

        mockMvc.perform(get("/api/orders/o1"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders/o1")
                        .header("Authorization", bearer("c1", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o1"));
        mockMvc.perform(get("/api/orders/o1")
                        .header("Authorization", bearer("a1", "ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void statusUpdatesAreAdminOnlyAndValidated() throws Exception {
        Order order = new Order();
        order.setId("o1");
        order.setOrderStatus("Shipped");
        when(orderService.updateOrderStatus(eq("o1"), eq("Shipped"), any(), eq("a1")))
                .thenReturn(order);

        String body = "{\"status\":\"Shipped\"}";

        mockMvc.perform(patch("/api/orders/o1/status")
                        .contentType("application/json").content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/api/orders/o1/status")
                        .header("Authorization", bearer("c1", "CUSTOMER"))
                        .contentType("application/json").content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/orders/o1/status")
                        .header("Authorization", bearer("a1", "ADMIN"))
                        .contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderStatus").value("Shipped"));
        mockMvc.perform(patch("/api/orders/o1/status")
                        .header("Authorization", bearer("a1", "ADMIN"))
                        .contentType("application/json").content("{\"status\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    private static String createTestSecret() {
        byte[] key = new byte[32];
        new SecureRandom().nextBytes(key);
        return Base64.getEncoder().encodeToString(key);
    }
}
