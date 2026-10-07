package in.algoworks.service;

import in.algoworks.entity.Order;
import in.algoworks.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Service contains the business logic for Orders
@Service
public class OrderService {

    private OrderRepository orderRepository;

    // Constructor injection
    // Spring automatically provides OrderRepository object
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Create a new order
    public Order createOrder(Order order) {

        // Save order into MongoDB
        return orderRepository.save(order);
    }

    // Get order by MongoDB ID
    public Order getOrder(String id) {

        // Search order by ID
        Optional<Order> order = orderRepository.findById(id);

        // Return order if found
        if (order.isPresent()) {
            return order.get();
        }

        // Return null if order does not exist
        return null;
    }

    // Get all orders
    public List<Order> getAllOrders() {

        // Fetch all orders from MongoDB
        return orderRepository.findAll();
    }

    // Update an existing order
    public Order updateOrder(String id, Order orderRequest) {

        // First find the existing order
        Optional<Order> existingOrder = orderRepository.findById(id);

        // If order does not exist, return null
        if (existingOrder.isEmpty()) {
            return null;
        }

        // Get existing order object
        Order orderToSave = existingOrder.get();

        // Update fields
        orderToSave.setOrderId(orderRequest.getOrderId());
        orderToSave.setCustomer(orderRequest.getCustomer());
        orderToSave.setProduct(orderRequest.getProduct());
        orderToSave.setAmount(orderRequest.getAmount());
        orderToSave.setPaymentStatus(orderRequest.getPaymentStatus());
        orderToSave.setOrderStatus(orderRequest.getOrderStatus());
        orderToSave.setDate(orderRequest.getDate());

        // Save updated order
        return orderRepository.save(orderToSave);
    }

    // Delete an order
    public Boolean deleteOrder(String id) {

        // Check whether order exists
        Boolean exists = orderRepository.existsById(id);

        // If order does not exist
        if (!exists) {
            return false;
        }

        // Delete order from MongoDB
        orderRepository.deleteById(id);

        return true;
    }
}