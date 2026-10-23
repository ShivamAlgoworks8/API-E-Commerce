
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
        Customer customer = findCustomer(orderRequest.getCustomer());
        Product product = findProduct(orderRequest.getProduct());
        requireAvailableStock(product);
        if (orderRequest.getAmount() != null
                && Double.compare(orderRequest.getAmount(), product.getPrice()) != 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Order amount must match the selected product price");
        }

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
        boolean customerChanged = !Objects.equals(order.getCustomer(), orderRequest.getCustomer());
        Customer customer = customerChanged ? findCustomer(orderRequest.getCustomer()) : null;
        boolean productChanged = !Objects.equals(order.getProduct(), orderRequest.getProduct());
        Product product = productChanged ? findProduct(orderRequest.getProduct()) : null;
        Product previousProduct = null;
        boolean wasStockDeducted = Boolean.TRUE.equals(order.getStockDeducted());
        boolean reserveNewProduct = wasStockDeducted && productChanged;

        if (wasStockDeducted && productChanged) {
            previousProduct = findProductIfPresent(order.getProduct());
        }

        if (reserveNewProduct && product != null) {
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
        if (productChanged) {
            order.setProduct(product.getName());
            order.setAmount(product.getPrice());
        }
        if (customerChanged) {
            order.setCustomer(customer.getName());
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
                ? findProduct(order.getProduct())
                : null;
        orderRepository.delete(order);
        if (product != null) {
            productRepository.incrementStock(product.getId());
        }

        return true;
    }

    private Customer findCustomer(String reference) {
        Customer customer = customerRepository.findById(reference).orElse(null);
        if (customer != null) {
            return customer;
        }

        List<Customer> customers = customerRepository.findAllByName(reference);
        if (customers.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Customer not found with id or name: " + reference);
        }
        if (customers.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer name is ambiguous: " + reference);
        }
        return customers.getFirst();
    }

    private Product findProduct(String reference) {
        Product product = findProductIfPresent(reference);
        if (product != null) {
            return product;
        }

        throw new ResourceNotFoundException(
                "Product not found with id or name: " + reference);
    }

    private Product findProductIfPresent(String reference) {
        Product product = productRepository.findById(reference).orElse(null);
        if (product != null) {
            return product;
        }

        List<Product> products = productRepository.findAllByName(reference);
        if (products.isEmpty()) {
            return null;
        }
        if (products.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product name is ambiguous: " + reference);
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
