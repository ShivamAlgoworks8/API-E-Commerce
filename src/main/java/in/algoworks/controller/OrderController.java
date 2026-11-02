package in.algoworks.controller;

import in.algoworks.dto.OrderStatusUpdateDTO;
import in.algoworks.entity.Order;
import in.algoworks.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;
import in.algoworks.dto.OrderRequestDTO;
// Handles HTTP requests related to Orders
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private OrderService orderService;

    // Constructor injection
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create a new order
    // POST /api/orders
    @PostMapping
    public Order createOrder(
            @Valid @RequestBody OrderRequestDTO orderRequest,
            Authentication authentication) {

        boolean admin = authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        if (admin && (orderRequest.getItems() == null || orderRequest.getItems().isEmpty())) {
            return orderService.createOrder(orderRequest);
        }

        String customerId = authentication != null
                ? authentication.getName()
                : orderRequest.getCustomer();

        return orderService.createOrder(orderRequest, customerId);
    }

    // Get all orders
    // GET /api/orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Get orders of the logged-in customer
    @GetMapping("/my-orders")
    public List<Order> getMyOrders() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String customerId = authentication.getName();

        return orderService.getCustomerOrders(customerId);
    }

    // Get order by ID
    // GET /api/orders/{id}
    // Admins can view any order; customers can only view their own.
    @GetMapping("/{id}")
    public Order getOrder(
            @PathVariable String id,
            Authentication authentication) {

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        return orderService.getOrderForUser(
                id, authentication.getName(), admin);
    }

    // Update only the tracking status of an order (Admin only)
    // PATCH /api/orders/{id}/status
    @PatchMapping("/{id}/status")
    public Order updateOrderStatus(
            @PathVariable String id,
            @Valid @RequestBody OrderStatusUpdateDTO statusUpdate,
            Authentication authentication) {

        return orderService.updateOrderStatus(
                id,
                statusUpdate.getStatus(),
                statusUpdate.getNote(),
                authentication.getName());
    }

    // Update an existing order
    // PUT /api/orders/{id}
    @PutMapping("/{id}")
    public Order updateOrder(
            @PathVariable String id,
            @Valid @RequestBody OrderRequestDTO orderRequest) {

        return orderService.updateOrder(id, orderRequest);
    }


    // Delete an order
    // DELETE /api/orders/{id}
    @DeleteMapping("/{id}")
    public Boolean deleteOrder(@PathVariable String id) {
        return orderService.deleteOrder(id);
    }
}
