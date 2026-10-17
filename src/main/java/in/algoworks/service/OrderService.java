
package in.algoworks.service;

import in.algoworks.dto.OrderRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.entity.Order;
import in.algoworks.entity.Product;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.CustomerRepository;
import in.algoworks.repository.OrderRepository;
import in.algoworks.repository.PaymentRepository;
import in.algoworks.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository,
                        CustomerRepository customerRepository,
                        ProductRepository productRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    // Create order
    public Order createOrder(OrderRequestDTO orderRequest) {
        Customer customer = findCustomerByName(orderRequest.getCustomer());
        Product product = findProductByName(orderRequest.getProduct());
        requireAvailableStock(product);

        Order order = new Order();

        order.setOrderId(nonBlank(orderRequest.getOrderId())
                ? orderRequest.getOrderId()
                : "ORD-" + UUID.randomUUID());
        ensureOrderIdAvailable(order.getOrderId(), null);
        order.setCustomer(customer.getName());
        order.setProduct(product.getName());
        order.setAmount(product.getPrice());
        order.setPaymentStatus(orderRequest.getPaymentStatus());
        order.setOrderStatus(orderRequest.getOrderStatus());
        order.setDate(orderRequest.getDate());
        order.setStockDeducted(true);

        reserveStock(product);
        try {
            return orderRepository.save(order);
        } catch (RuntimeException exception) {
            restoreStock(product, exception);
            throw exception;
        }
    }

    // Get order by MongoDB ID
    public Order getOrder(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id: " + id));
    }

    // Get all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // Update order
    public Order updateOrder(String id, OrderRequestDTO orderRequest) {
        Order order = getOrder(id);
        Customer customer = findCustomerByName(orderRequest.getCustomer());
        Product product = findProductByName(orderRequest.getProduct());
        Product previousProduct = null;
        boolean wasStockDeducted = Boolean.TRUE.equals(order.getStockDeducted());
        boolean productChanged = !Objects.equals(order.getProduct(), product.getName());
        boolean reserveNewProduct = wasStockDeducted && productChanged;

        if (wasStockDeducted && productChanged) {
            previousProduct = findProductByName(order.getProduct());
        }

        if (reserveNewProduct) {
            requireAvailableStock(product);
        }

        if (nonBlank(orderRequest.getOrderId())
                && !orderRequest.getOrderId().equals(order.getOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order ID cannot be changed because payments reference it");
        } else if (!nonBlank(order.getOrderId())) {
            order.setOrderId("ORD-" + UUID.randomUUID());
        }
        order.setCustomer(customer.getName());
        order.setProduct(product.getName());
        if (productChanged) {
            order.setAmount(product.getPrice());
        }
        order.setPaymentStatus(orderRequest.getPaymentStatus());
        order.setOrderStatus(orderRequest.getOrderStatus());
        order.setDate(orderRequest.getDate());

        if (reserveNewProduct) {
            reserveStock(product);
            order.setStockDeducted(true);
        }
        Order updatedOrder;
        try {
            updatedOrder = orderRepository.save(order);
        } catch (RuntimeException exception) {
            if (reserveNewProduct) {
                restoreStock(product, exception);
            }
            throw exception;
        }
        if (wasStockDeducted && productChanged && previousProduct != null) {
            productRepository.incrementStock(previousProduct.getId());
        }
        return updatedOrder;
    }

    // Delete order
    public Boolean deleteOrder(String id) {
        Order order = getOrder(id);
        if (paymentRepository.existsByOrderId(order.getOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot be deleted while payments reference it");
        }
        Product product = Boolean.TRUE.equals(order.getStockDeducted())
                ? findProductByName(order.getProduct())
                : null;
        orderRepository.delete(order);
        if (product != null) {
            productRepository.incrementStock(product.getId());
        }

        return true;
    }

    private Customer findCustomerByName(String name) {
        List<Customer> customers = customerRepository.findAllByName(name);
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Customer not found with name: " + name);
        }
        if (customers.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer name is ambiguous: " + name);
        }
        return customers.getFirst();
    }

    private Product findProductByName(String name) {
        List<Product> products = productRepository.findAllByName(name);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Product not found with name: " + name);
        }
        if (products.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product name is ambiguous: " + name);
        }
        return products.getFirst();
    }

    private void requireAvailableStock(Product product) {
        if (product.getStock() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is out of stock: " + product.getName());
        }
    }

    private void reserveStock(Product product) {
        if (productRepository.decrementStockIfAvailable(product.getId()) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product is out of stock: " + product.getName());
        }
    }

    private void restoreStock(Product product, RuntimeException originalException) {
        try {
            productRepository.incrementStock(product.getId());
        } catch (RuntimeException compensationException) {
            originalException.addSuppressed(compensationException);
        }
    }

    private void ensureOrderIdAvailable(String orderId, String currentMongoId) {
        orderRepository.findByOrderId(orderId).ifPresent(existing -> {
            if (!Objects.equals(existing.getId(), currentMongoId)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Order ID already exists: " + orderId);
            }
        });
    }

    private boolean nonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
