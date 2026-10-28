package in.algoworks.service;

import in.algoworks.dto.OrderRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.entity.Order;
import in.algoworks.entity.Product;
import in.algoworks.repository.CustomerRepository;
import in.algoworks.repository.OrderRepository;
import in.algoworks.repository.PaymentRepository;
import in.algoworks.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private ProductRepository productRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderRepository = mock(OrderRepository.class);
        customerRepository = mock(CustomerRepository.class);
        productRepository = mock(ProductRepository.class);
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        orderService = new OrderService(
                orderRepository, customerRepository, productRepository, paymentRepository);
    }

    @Test
    void createsOrderFromMongoIdsAndUsesCatalogPriceWhenAmountIsOmitted() {
        Customer customer = new Customer();
        customer.setId("customer-mongo-id");
        customer.setName("A Customer");
        Product product = new Product();
        product.setId("product-mongo-id");
        product.setName("A Product");
        product.setPrice(24.50);
        product.setStock(3);

        when(customerRepository.findById("customer-mongo-id"))
                .thenReturn(Optional.of(customer));
        when(productRepository.findById("product-mongo-id"))
                .thenReturn(Optional.of(product));
        when(orderRepository.findByOrderId(anyString())).thenReturn(Optional.empty());
        when(productRepository.decrementStockIfAvailable("product-mongo-id")).thenReturn(1L);
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomer("customer-mongo-id");
        request.setProduct("product-mongo-id");
        request.setOrderStatus("Processing");

        Order created = orderService.createOrder(request);

        assertEquals("A Customer", created.getCustomer());
        assertEquals("A Product", created.getProduct());
        assertEquals(24.50, created.getAmount());
        verify(productRepository).decrementStockIfAvailable("product-mongo-id");
    }

    @Test
    void restoresReservedStockWhenMongoOrderSaveFails() {
        Customer customer = new Customer();
        customer.setId("customer-mongo-id");
        customer.setName("A Customer");
        Product product = new Product();
        product.setId("product-mongo-id");
        product.setName("A Product");
        product.setPrice(24.50);
        product.setStock(3);

        when(customerRepository.findById("customer-mongo-id"))
                .thenReturn(Optional.of(customer));
        when(productRepository.findById("product-mongo-id"))
                .thenReturn(Optional.of(product));
        when(orderRepository.findByOrderId(anyString())).thenReturn(Optional.empty());
        when(productRepository.decrementStockIfAvailable("product-mongo-id")).thenReturn(1L);
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new IllegalStateException("Mongo write failed"));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomer("customer-mongo-id");
        request.setProduct("product-mongo-id");
        request.setOrderStatus("Processing");

        assertThrows(IllegalStateException.class, () -> orderService.createOrder(request));
        verify(productRepository).incrementStock("product-mongo-id");
    }

    @Test
    void updatesOrderDetailsWithoutRequiringUnchangedCatalogReferencesToExist() {
        Order existing = new Order();
        existing.setId("order-mongo-id");
        existing.setOrderId("ORD-1001");
        existing.setCustomer("A former customer");
        existing.setProduct("A discontinued product");
        existing.setAmount(24.50);
        existing.setPaymentStatus("Paid");
        existing.setOrderStatus("Processing");
        existing.setDate("2026-10-01");
        existing.setStockDeducted(true);

        when(orderRepository.findById("order-mongo-id")).thenReturn(Optional.of(existing));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setOrderId("ORD-1001");
        request.setCustomer("A former customer");
        request.setProduct("A discontinued product");
        request.setAmount(24.50);
        request.setPaymentStatus("Pending");
        request.setOrderStatus("Delivered");
        request.setDate("2026-10-15");

        Order updated = orderService.updateOrder("order-mongo-id", request);

        assertEquals("A former customer", updated.getCustomer());
        assertEquals("A discontinued product", updated.getProduct());
        assertEquals("Pending", updated.getPaymentStatus());
        assertEquals("Delivered", updated.getOrderStatus());
        assertEquals("2026-10-15", updated.getDate());
        verify(customerRepository, never()).findById(anyString());
        verify(customerRepository, never()).findAllByName(anyString());
        verify(productRepository, never()).findById(anyString());
        verify(productRepository, never()).findAllByName(anyString());
    }

    @Test
    void updatesCustomerNameWithoutRequiringUnchangedProductToExist() {
        Order existing = new Order();
        existing.setId("order-mongo-id");
        existing.setOrderId("ORD-1001");
        existing.setCustomer("Previous customer");
        existing.setProduct("A discontinued product");
        existing.setAmount(24.50);

        Customer newCustomer = new Customer();
        newCustomer.setId("customer-mongo-id");
        newCustomer.setName("New customer");
        when(orderRepository.findById("order-mongo-id")).thenReturn(Optional.of(existing));
        when(customerRepository.findAllByName("New customer")).thenReturn(List.of(newCustomer));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OrderRequestDTO request = new OrderRequestDTO();
        request.setOrderId("ORD-1001");
        request.setCustomer("New customer");
        request.setProduct("A discontinued product");
        request.setAmount(24.50);
        request.setPaymentStatus("Paid");
        request.setOrderStatus("Processing");
        request.setDate("2026-10-01");

        Order updated = orderService.updateOrder("order-mongo-id", request);

        assertEquals("New customer", updated.getCustomer());
        assertEquals("A discontinued product", updated.getProduct());
        verify(productRepository, never()).findById(anyString());
        verify(productRepository, never()).findAllByName(anyString());
    }
}
