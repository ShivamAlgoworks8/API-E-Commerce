package in.algoworks.controller;

import in.algoworks.entity.Order;
import in.algoworks.service.OrderService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

// Allow frontend running on port 3001 to access Order APIs
@CrossOrigin(origins = "http://localhost:3001")
public class OrderController {

    private OrderService orderService;

    // Constructor injection
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create a new order
    // POST /api/orders
    @PostMapping
    public Order createOrder(@Valid @RequestBody OrderRequestDTO orderRequest){
        return orderService.createOrder(orderRequest);
    }

    // Get all orders
    // GET /api/orders
    @GetMapping
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    // Get order by ID
    // GET /api/orders/{id}
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderService.getOrder(id);
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