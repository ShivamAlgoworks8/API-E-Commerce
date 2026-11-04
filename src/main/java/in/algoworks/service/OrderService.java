
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    // Inject the repositories required for order, customer, product and payment
    // operations.
    public OrderService(
            OrderRepository orderRepository,
            CustomerRepository customerRepository,
            ProductRepository productRepository,
            PaymentRepository paymentRepository) {

        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    // Legacy overload for single-product order creation when customer is in the
    // DTO.
    public Order createOrder(OrderRequestDTO orderRequest) {
        if (orderRequest == null) {
            throw badRequest("Order request is required.");
        }

        // If multi-product checkout fields are present, delegate to the full checkout
        // flow.
        if (orderRequest.getItems() != null && !orderRequest.getItems().isEmpty()) {
            return createOrder(orderRequest, orderRequest.getCustomer());
        }

        Customer customer = findCustomer(orderRequest.getCustomer());
        requireActiveCustomer(customer);

        Product product = findProduct(orderRequest.getProduct());
        int quantity = orderRequest.getQuantity() == null ? 1 : orderRequest.getQuantity();
        if (quantity <= 0) {
            throw badRequest("Quantity must be greater than zero.");
        }
        if (product.getStock() < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock for product: " + product.getName());
        }

        double totalAmount = orderRequest.getAmount() != null
                ? orderRequest.getAmount()
                : product.getPrice() * quantity;

        Order order = new Order();
        String orderId = nonBlank(orderRequest.getOrderId())
                ? orderRequest.getOrderId()
                : "ORD-" + UUID.randomUUID();
        ensureOrderIdAvailable(orderId, null);

        order.setOrderId(orderId);
        order.setCustomer(customer.getName());
        order.setCustomerId(customer.getId());
        order.setProduct(product.getName());
        order.setQuantity(quantity);
        order.setAmount(totalAmount);
        order.setPaymentStatus(
                nonBlank(orderRequest.getPaymentStatus())
                        ? orderRequest.getPaymentStatus()
                        : "Pending");
        String initialStatus = nonBlank(orderRequest.getOrderStatus())
                ? parseStatus(orderRequest.getOrderStatus()).getLabel()
                : OrderStatus.PENDING.getLabel();
        order.setOrderStatus(initialStatus);
        String now = nonBlank(orderRequest.getDate())
                ? orderRequest.getDate()
                : LocalDateTime.now().toString();
        order.setDate(now);
        order.setUpdatedAt(now);
        order.setStatusHistory(new ArrayList<>(List.of(
                new Order.StatusHistory(
                        initialStatus,
                        now,
                        "Order placed",
                        customer.getId()))));
        order.setStockDeducted(true);

        long updatedCount = quantity == 1
                ? productRepository.decrementStockIfAvailable(product.getId())
                : productRepository.decrementStockByQuantityIfAvailable(
                        product.getId(), quantity, -quantity);

        if (updatedCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient stock for product: " + product.getName());
        }

        try {
            return orderRepository.save(order);
        } catch (RuntimeException exception) {
            try {
                if (quantity == 1) {
                    productRepository.incrementStock(product.getId());
                } else {
                    productRepository.incrementStockByQuantity(product.getId(), quantity);
                }
            } catch (RuntimeException compensationException) {
                exception.addSuppressed(compensationException);
            }
            throw exception;
        }
    }

    // Create an order containing all the products selected during checkout.
    public Order createOrder(OrderRequestDTO orderRequest, String customerId) {

        if (orderRequest == null) {
            throw badRequest("Order request is required.");
        }

        Customer customer = findCustomer(customerId);

        // Inactive customers are not allowed to place orders.
        requireActiveCustomer(customer);

        OrderRequestDTO.DeliveryAddressDTO address = orderRequest.getDeliveryAddress();

        if (address == null) {
            throw badRequest("Delivery address is required.");
        }

        // Validate the delivery address before processing the order.
        validateAddress(address);

        String paymentMethod = orderRequest.getPaymentMethod();

        // Only the payment methods currently supported by checkout are accepted.
        if (paymentMethod == null ||
                !("COD".equalsIgnoreCase(paymentMethod)
                        || "ONLINE".equalsIgnoreCase(paymentMethod))) {
            throw badRequest("Payment method must be COD or ONLINE.");
        }

        List<OrderRequestDTO.OrderItemRequestDTO> requestedItems = orderRequest.getItems();

        if (requestedItems == null || requestedItems.isEmpty()) {
            throw badRequest("At least one product is required.");
        }

        Map<String, Product> products = new LinkedHashMap<>();
        Map<String, Integer> quantities = new LinkedHashMap<>();

        // Resolve each product from the database and combine duplicate cart entries.
        for (OrderRequestDTO.OrderItemRequestDTO item : requestedItems) {

            if (item == null ||
                    item.getProduct() == null ||
                    item.getProduct().isBlank()) {
                throw badRequest("Every order item must have a product.");
            }

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw badRequest(
                        "Every product quantity must be greater than zero.");
            }

            Product product = findProduct(item.getProduct());

            try {
                quantities.merge(
                        product.getId(),
                        item.getQuantity(),
                        Math::addExact);
            } catch (ArithmeticException exception) {
                throw badRequest("Product quantity is too large.");
            }

            products.put(product.getId(), product);
        }

        double subtotal = 0.0;
        int totalQuantity = 0;

        List<Order.OrderItem> orderItems = new ArrayList<>();

        // Calculate prices from database product records instead of trusting frontend
        // totals.
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {

            Product product = products.get(entry.getKey());
            int quantity = entry.getValue();

            if (product.getPrice() < 0) {
                throw badRequest(
                        "Invalid price for product: " + product.getName());
            }

            // Check stock before attempting to reserve the products.
            if (product.getStock() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: " + product.getName());
            }

            double price = product.getPrice();
            double itemSubtotal = price * quantity;

            if (!Double.isFinite(itemSubtotal)) {
                throw badRequest("Invalid order amount.");
            }

            subtotal += itemSubtotal;

            try {
                totalQuantity = Math.addExact(totalQuantity, quantity);
            } catch (ArithmeticException exception) {
                throw badRequest("Total order quantity is too large.");
            }

            // Store the product name, quantity and database price in the order.
            orderItems.add(
                    new Order.OrderItem(product.getName(), quantity, price));
        }

        // Apply the same shipping rule used by the checkout page.
        double shipping = subtotal >= 999.0 ? 0.0 : 99.0;
        double totalAmount = subtotal + shipping;

        if (!Double.isFinite(totalAmount) || totalAmount <= 0) {
            throw badRequest("Invalid order total.");
        }

        Order order = new Order();

        // Generate an order ID when the request does not provide one.
        String orderId = nonBlank(orderRequest.getOrderId())
                ? orderRequest.getOrderId()
                : "ORD-" + UUID.randomUUID();

        ensureOrderIdAvailable(orderId, null);

        order.setOrderId(orderId);
        // Use the customer name entered in the Checkout form (without overwriting it
        // with the account name).
        String checkoutCustomerName = nonBlank(address.getFullName())
                ? address.getFullName().trim()
                : nonBlank(orderRequest.getCustomer())
                        && !Objects.equals(orderRequest.getCustomer().trim(), customer.getId())
                                ? orderRequest.getCustomer().trim()
                                : customer.getName();
        order.setCustomer(checkoutCustomerName);
        order.setCustomerId(customer.getId());
        order.setItems(orderItems);

        // Retain the legacy fields for existing Admin screens.
        order.setProduct(orderItems.get(0).getProduct());
        order.setQuantity(totalQuantity);

        order.setAmount(totalAmount);
        order.setPaymentMethod(paymentMethod.toUpperCase());

        // No payment gateway is integrated, so online payments remain pending.
        order.setPaymentStatus("Pending");
        order.setOrderStatus(OrderStatus.PENDING.getLabel());

        Order.DeliveryAddress deliveryAddress = new Order.DeliveryAddress();

        deliveryAddress.setFullName(address.getFullName().trim());
        deliveryAddress.setPhone(address.getPhone().trim());
        deliveryAddress.setStreet(address.getStreet().trim());
        deliveryAddress.setCity(address.getCity().trim());
        deliveryAddress.setState(address.getState().trim());
        deliveryAddress.setPincode(address.getPincode().trim());

        order.setDeliveryAddress(deliveryAddress);
        order.setDate(LocalDateTime.now().toString());
        order.setUpdatedAt(order.getDate());
        order.setStatusHistory(new ArrayList<>(List.of(
                new Order.StatusHistory(
                        OrderStatus.PENDING.getLabel(),
                        order.getDate(),
                        "Order placed",
                        customer.getId()))));
        order.setStockDeducted(true);

        // Keep track of stock deductions so they can be reversed if saving fails.
        List<String> deductedProductIds = new ArrayList<>();

        try {
            for (Map.Entry<String, Integer> entry : quantities.entrySet()) {

                Product product = products.get(entry.getKey());
                int quantity = entry.getValue();

                // Atomically deduct stock only when sufficient stock remains.
                long updatedCount = productRepository.decrementStockByQuantityIfAvailable(
                        product.getId(),
                        quantity,
                        -quantity);

                if (updatedCount == 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Insufficient stock for product: "
                                    + product.getName());
                }

                deductedProductIds.add(product.getId());
            }

            // Save the completed order to the MongoDB orders collection.
            return orderRepository.save(order);

        } catch (RuntimeException exception) {

            // Restore previously deducted stock if any part of order creation fails.
            for (String productId : deductedProductIds) {
                try {
                    productRepository.incrementStockByQuantity(
                            productId,
                            quantities.get(productId));
                } catch (RuntimeException compensationException) {
                    exception.addSuppressed(compensationException);
                }
            }

            throw exception;
        }
    }

    // Find an order using its MongoDB document ID.
    public Order getOrder(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with id: " + id));
    }

    // Return all orders for the Admin Panel, newest first.
    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>(orderRepository.findAll());

        orders.sort((first, second) -> {
            int dateCmp = String.valueOf(second.getDate())
                    .compareTo(String.valueOf(first.getDate()));
            if (dateCmp != 0) {
                return dateCmp;
            }
            return String.valueOf(second.getId())
                    .compareTo(String.valueOf(first.getId()));
        });

        return orders;
    }

    // Return a customer's own orders, newest first.
    public List<Order> getCustomerOrders(String customerId) {
        List<Order> orders = new ArrayList<>(
                orderRepository.findByCustomerId(customerId));

        orders.sort((first, second) -> {
            int dateCmp = String.valueOf(second.getDate())
                    .compareTo(String.valueOf(first.getDate()));
            if (dateCmp != 0) {
                return dateCmp;
            }
            return String.valueOf(second.getId())
                    .compareTo(String.valueOf(first.getId()));
        });

        return orders;
    }

    // Return one order if the caller is an admin or the customer who placed it.
    public Order getOrderForUser(String id, String userId, boolean admin) {
        Order order = getOrder(id);

        if (!admin && !Objects.equals(order.getCustomerId(), userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not allowed to view this order.");
        }

        return order;
    }

    // Move an order to a new tracking status and record it in the timeline.
    public Order updateOrderStatus(
            String id, String status, String note, String updatedBy) {

        Order order = getOrder(id);
        OrderStatus target = parseStatus(status);

        if (target.getLabel().equalsIgnoreCase(order.getOrderStatus())) {
            throw badRequest("Order is already " + target.getLabel() + ".");
        }

        changeStatus(order, target, note, updatedBy);

        return orderRepository.save(order);
    }

    // Update order details while preserving stock and product data for checkout
    // orders.
    public Order updateOrder(String id, OrderRequestDTO orderRequest) {

        Order order = getOrder(id);

        if (orderRequest == null) {
            throw badRequest("Order update request is required.");
        }

        // Keep the order ID unchanged because payment records may reference it.
        if (nonBlank(orderRequest.getOrderId())
                && !orderRequest.getOrderId().equals(order.getOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order ID cannot be changed because payments may reference it.");
        }

        // Status changes go through the same validation and history as the status API.
        if (nonBlank(orderRequest.getOrderStatus())
                && !orderRequest.getOrderStatus().trim()
                        .equalsIgnoreCase(order.getOrderStatus())) {

            changeStatus(
                    order,
                    parseStatus(orderRequest.getOrderStatus()),
                    null,
                    "ADMIN");
        }

        // For multi-product orders, update only administrative/payment fields here.
        if (order.getItems() != null && !order.getItems().isEmpty()) {

            if (nonBlank(orderRequest.getCustomer())) {
                String updatedCustomerName = orderRequest.getCustomer().trim();
                order.setCustomer(updatedCustomerName);
                if (order.getDeliveryAddress() != null) {
                    order.getDeliveryAddress().setFullName(updatedCustomerName);
                }
            }

            if (nonBlank(orderRequest.getPaymentStatus())) {
                order.setPaymentStatus(orderRequest.getPaymentStatus());
            }

            if (nonBlank(orderRequest.getDate())) {
                order.setDate(orderRequest.getDate());
            }

            if (nonBlank(orderRequest.getPaymentMethod())) {
                String method = orderRequest.getPaymentMethod();

                if (!"COD".equalsIgnoreCase(method)
                        && !"ONLINE".equalsIgnoreCase(method)) {
                    throw badRequest("Payment method must be COD or ONLINE.");
                }

                order.setPaymentMethod(method.toUpperCase());
            }

            return orderRepository.save(order);
        }

        // Preserve support for older single-product orders.
        boolean customerChanged = !Objects.equals(
                order.getCustomer(),
                orderRequest.getCustomer());

        Customer customer = customerChanged
                ? findCustomer(orderRequest.getCustomer())
                : null;

        if (customerChanged) {
            requireActiveCustomer(customer);
        }

        boolean productChanged = nonBlank(orderRequest.getProduct())
                && !Objects.equals(
                        order.getProduct(),
                        orderRequest.getProduct());

        Product newProduct = productChanged
                ? findProduct(orderRequest.getProduct())
                : null;

        Product oldProduct = null;

        boolean stockWasDeducted = Boolean.TRUE.equals(order.getStockDeducted());

        boolean reserveNewProduct = stockWasDeducted && productChanged;

        int oldQuantity = Math.max(1, order.getQuantity());

        int newQuantity = orderRequest.getQuantity() == null
                ? oldQuantity
                : orderRequest.getQuantity();

        if (newQuantity <= 0) {
            throw badRequest("Quantity must be greater than zero.");
        }

        if (reserveNewProduct) {
            oldProduct = findProductIfPresent(order.getProduct());

            if (newProduct.getStock() < newQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: "
                                + newProduct.getName());
            }
        } else if (stockWasDeducted
                && orderRequest.getQuantity() != null
                && newQuantity > oldQuantity) {

            Product currentProduct = findProduct(order.getProduct());
            int additionalQuantity = newQuantity - oldQuantity;

            if (currentProduct.getStock() < additionalQuantity) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Insufficient stock for product: "
                                + currentProduct.getName());
            }
        }

        if (customerChanged) {
            order.setCustomer(customer.getName());
        }

        if (productChanged) {
            order.setProduct(newProduct.getName());
        }

        if (orderRequest.getQuantity() != null) {
            order.setQuantity(newQuantity);
        }

        if (productChanged || orderRequest.getQuantity() != null) {
            Product currentProduct = productChanged
                    ? newProduct
                    : findProduct(order.getProduct());

            order.setAmount(currentProduct.getPrice() * newQuantity);
        }

        if (nonBlank(orderRequest.getPaymentStatus())) {
            order.setPaymentStatus(orderRequest.getPaymentStatus());
        }

        if (nonBlank(orderRequest.getDate())) {
            order.setDate(orderRequest.getDate());
        }

        boolean newStockReserved = false;

        try {
            // Reserve stock for the replacement product when required.
            if (reserveNewProduct) {
                if (productRepository.decrementStockByQuantityIfAvailable(
                        newProduct.getId(),
                        newQuantity,
                        -newQuantity) == 0) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Insufficient stock for product: "
                                    + newProduct.getName());
                }

                newStockReserved = true;

            } else if (stockWasDeducted
                    && newQuantity != oldQuantity) {

                Product currentProduct = findProduct(order.getProduct());
                int difference = newQuantity - oldQuantity;

                // Deduct extra units or return units when the quantity changes.
                if (difference > 0) {
                    if (productRepository.decrementStockByQuantityIfAvailable(
                            currentProduct.getId(),
                            difference,
                            -difference) == 0) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Insufficient stock for product: "
                                        + currentProduct.getName());
                    }
                } else {
                    productRepository.incrementStockByQuantity(
                            currentProduct.getId(),
                            -difference);
                }
            }

            Order updatedOrder = orderRepository.save(order);

            // Return the old product's stock after the replacement order is saved.
            if (reserveNewProduct && oldProduct != null) {
                productRepository.incrementStockByQuantity(
                        oldProduct.getId(),
                        oldQuantity);
            }

            return updatedOrder;

        } catch (RuntimeException exception) {

            // Undo the new product reservation if saving the update fails.
            if (newStockReserved) {
                try {
                    productRepository.incrementStockByQuantity(
                            newProduct.getId(),
                            newQuantity);
                } catch (RuntimeException compensationException) {
                    exception.addSuppressed(compensationException);
                }
            }

            throw exception;
        }
    }

    // Delete an order and return its reserved stock.
    public Boolean deleteOrder(String id) {

        Order order = getOrder(id);

        // Keep orders that are referenced by payment records.
        if (paymentRepository.existsByOrderId(order.getOrderId())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Order cannot be deleted while payments reference it.");
        }

        restoreStock(order);

        orderRepository.delete(order);
        return true;
    }

    private OrderStatus parseStatus(String status) {
        return OrderStatus.parse(status).orElseThrow(() -> badRequest("Invalid order status. Allowed values: "
                + OrderStatus.allowedLabels()));
    }

    // Validate the transition, update the status and append a timeline entry.
    private void changeStatus(
            Order order, OrderStatus target, String note, String updatedBy) {

        String current = order.getOrderStatus();
        OrderStatus currentStatus = OrderStatus.parse(current).orElse(null);

        boolean allowed = currentStatus != null
                ? currentStatus.canTransitionTo(target)
                // Legacy statuses such as "Returned" are final.
                : !"Returned".equalsIgnoreCase(current);

        if (!allowed) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot change order status from "
                            + current + " to " + target.getLabel() + ".");
        }

        String now = LocalDateTime.now().toString();

        if (order.getStatusHistory() == null) {
            order.setStatusHistory(new ArrayList<>());
        }

        // Start the timeline with the original status for orders created before
        // tracking.
        if (order.getStatusHistory().isEmpty() && nonBlank(current)) {
            order.getStatusHistory().add(new Order.StatusHistory(
                    currentStatus != null ? currentStatus.getLabel() : current,
                    order.getDate(),
                    "Order placed",
                    null));
        }

        if (target == OrderStatus.CANCELLED) {
            restoreStock(order);
        }

        order.setOrderStatus(target.getLabel());
        order.setUpdatedAt(now);
        order.getStatusHistory().add(new Order.StatusHistory(
                target.getLabel(),
                now,
                nonBlank(note) ? note.trim() : null,
                updatedBy));
    }

    // Return reserved stock to the catalog once for cancelled or deleted orders.
    private void restoreStock(Order order) {

        if (!Boolean.TRUE.equals(order.getStockDeducted())) {
            return;
        }

        if (order.getItems() != null && !order.getItems().isEmpty()) {

            for (Order.OrderItem item : order.getItems()) {
                Product product = findProductIfPresent(item.getProduct());

                if (product != null) {
                    productRepository.incrementStockByQuantity(
                            product.getId(),
                            item.getQuantity());
                }
            }

        } else if (nonBlank(order.getProduct())) {

            // Restore stock for older single-product orders.
            Product product = findProductIfPresent(order.getProduct());

            if (product != null) {
                productRepository.incrementStockByQuantity(
                        product.getId(),
                        Math.max(1, order.getQuantity()));
            }
        }

        order.setStockDeducted(false);
    }

    // Find a customer by MongoDB ID or by a unique customer name.
    private Customer findCustomer(String reference) {

        if (!nonBlank(reference)) {
            throw badRequest("Customer is required.");
        }

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

        return customers.get(0);
    }

    // Check customer status before creating an order or changing its customer.
    private void requireActiveCustomer(Customer customer) {
        if (customer.getStatus() != null
                && !"Active".equalsIgnoreCase(customer.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is inactive. Please contact the administrator.");
        }
    }

    // Find a product by MongoDB ID or by a unique product name.
    private Product findProduct(String reference) {

        Product product = findProductIfPresent(reference);

        if (product != null) {
            return product;
        }

        throw new ResourceNotFoundException(
                "Product not found with id or name: " + reference);
    }

    private Product findProductIfPresent(String reference) {

        if (!nonBlank(reference)) {
            return null;
        }

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

        return products.get(0);
    }

    // Validate required address fields and Indian phone/PIN formats.
    private void validateAddress(
            OrderRequestDTO.DeliveryAddressDTO address) {

        if (!nonBlank(address.getFullName())
                || !nonBlank(address.getPhone())
                || !nonBlank(address.getStreet())
                || !nonBlank(address.getCity())
                || !nonBlank(address.getState())
                || !nonBlank(address.getPincode())) {

            throw badRequest("All delivery address fields are required.");
        }

        if (!address.getPhone().trim().matches("^[6-9]\\d{9}$")) {
            throw badRequest(
                    "Please enter a valid 10-digit Indian mobile number.");
        }

        if (!address.getPincode().trim().matches("^\\d{6}$")) {
            throw badRequest("Please enter a valid 6-digit PIN code.");
        }
    }

    // Prevent duplicate business order IDs.
    private void ensureOrderIdAvailable(
            String orderId,
            String currentMongoId) {

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

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message);
    }
}
