package in.algoworks.service;

import in.algoworks.dto.PaymentRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.entity.Order;
import in.algoworks.entity.Payment;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.CustomerRepository;
import in.algoworks.repository.OrderRepository;
import in.algoworks.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private PaymentRepository paymentRepository;
    private OrderRepository orderRepository;
    private CustomerRepository customerRepository;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        orderRepository = mock(OrderRepository.class);
        customerRepository = mock(CustomerRepository.class);
        paymentService = new PaymentService(
                paymentRepository, orderRepository, customerRepository);
    }

    @Test
    void createsPaymentFromMongoOrderIdAndUsesOrderAmountWhenOmitted() {
        Order order = order("order-mongo-id", "ORD-1001", "A Customer", 24.50);
        Customer customer = customer("customer-mongo-id", "A Customer");
        when(orderRepository.findByOrderId("order-mongo-id")).thenReturn(Optional.empty());
        when(orderRepository.findById("order-mongo-id")).thenReturn(Optional.of(order));
        when(customerRepository.findById("customer-mongo-id"))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByPaymentId(anyString())).thenReturn(false);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId("order-mongo-id");
        request.setCustomerId("customer-mongo-id");
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");

        Payment created = paymentService.createPayment(request);

        assertEquals("ORD-1001", created.getOrderId());
        assertEquals("customer-mongo-id", created.getCustomerId());
        assertEquals("A Customer", created.getCustomerName());
        assertEquals(24.50, created.getPaymentAmount());
    }

    @Test
    void rejectsPaymentAmountThatDoesNotMatchOrder() {
        Order order = order("order-mongo-id", "ORD-1001", "A Customer", 24.50);
        Customer customer = customer("customer-mongo-id", "A Customer");
        when(orderRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(order));
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.empty());
        when(customerRepository.findById("customer-mongo-id"))
                .thenReturn(Optional.of(customer));

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId("ORD-1001");
        request.setCustomerId("customer-mongo-id");
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");
        request.setPaymentAmount(20.00);

        assertThrows(ResponseStatusException.class,
                () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void rejectsDuplicateBusinessPaymentId() {
        Order order = order("order-mongo-id", "ORD-1001", "A Customer", 24.50);
        Customer customer = customer("customer-mongo-id", "A Customer");
        when(orderRepository.findByOrderId("ORD-1001")).thenReturn(Optional.of(order));
        when(orderRepository.findById("ORD-1001")).thenReturn(Optional.empty());
        when(customerRepository.findById("customer-mongo-id"))
                .thenReturn(Optional.of(customer));
        when(paymentRepository.existsByPaymentId("PAY-1001")).thenReturn(true);

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentId("PAY-1001");
        request.setOrderId("ORD-1001");
        request.setCustomerId("customer-mongo-id");
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");

        assertThrows(ResponseStatusException.class,
                () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void rejectsMissingOrderOrCustomerInsteadOfPersistingUnlinkedPayment() {
        when(orderRepository.findByOrderId("missing-order")).thenReturn(Optional.empty());
        when(orderRepository.findById("missing-order")).thenReturn(Optional.empty());

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId("missing-order");
        request.setCustomerId("customer-mongo-id");
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");

        assertThrows(ResourceNotFoundException.class,
                () -> paymentService.createPayment(request));
        verify(customerRepository, never()).findById(anyString());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void updatePaymentPreservesAmountWhenOmitted() {
        Payment payment = new Payment();
        payment.setId("payment-mongo-id");
        payment.setOrderId("ORD-1001");
        payment.setPaymentAmount(24.50);
        when(paymentRepository.findById("payment-mongo-id"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentStatus("Pending");
        request.setPaymentType("Card");

        Payment updated = paymentService.updatePayment(
                "payment-mongo-id", request);

        assertEquals(24.50, updated.getPaymentAmount());
        verify(orderRepository, never()).findByOrderId(anyString());
    }

    @Test
    void rejectsPaymentUpdateAmountThatDoesNotMatchOrder() {
        Payment payment = new Payment();
        payment.setId("payment-mongo-id");
        payment.setOrderId("ORD-1001");
        payment.setPaymentAmount(24.50);
        when(paymentRepository.findById("payment-mongo-id"))
                .thenReturn(Optional.of(payment));
        when(orderRepository.findByOrderId("ORD-1001"))
                .thenReturn(Optional.of(order(
                        "order-mongo-id", "ORD-1001", "A Customer", 24.50)));
        when(orderRepository.findById("ORD-1001"))
                .thenReturn(Optional.empty());

        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");
        request.setPaymentAmount(10.00);

        assertThrows(ResponseStatusException.class,
                () -> paymentService.updatePayment("payment-mongo-id", request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    private static Order order(
            String mongoId, String orderId, String customerName, double amount) {
        Order order = new Order();
        order.setId(mongoId);
        order.setOrderId(orderId);
        order.setCustomer(customerName);
        order.setAmount(amount);
        return order;
    }

    private static Customer customer(String mongoId, String name) {
        Customer customer = new Customer();
        customer.setId(mongoId);
        customer.setName(name);
        return customer;
    }
}
