
package in.algoworks.service;

import in.algoworks.dto.PaymentRequestDTO;
import in.algoworks.entity.Customer;
import in.algoworks.entity.Order;
import in.algoworks.entity.Payment;
import in.algoworks.exception.ResourceNotFoundException;
import in.algoworks.repository.CustomerRepository;
import in.algoworks.repository.OrderRepository;
import in.algoworks.repository.PaymentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          CustomerRepository customerRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
    }

    // Create payment
    public Payment createPayment(PaymentRequestDTO paymentRequest) {
        Order order = findOrderByBusinessId(paymentRequest.getOrderId());
        Customer customer = findCustomer(paymentRequest.getCustomerId());
        validateCustomerMatchesOrder(customer, order, paymentRequest.getCustomerName());

        Payment payment = new Payment();

        payment.setPaymentId(nonBlank(paymentRequest.getPaymentId())
                ? paymentRequest.getPaymentId()
                : "PAY-" + UUID.randomUUID());
        payment.setOrderId(order.getOrderId());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus());
        payment.setPaymentType(paymentRequest.getPaymentType());
        payment.setPaymentDate(paymentRequest.getPaymentDate());
        payment.setPaymentTime(paymentRequest.getPaymentTime());
        payment.setPaymentAmount(paymentRequest.getPaymentAmount());
        payment.setCustomerId(customer.getId());
        payment.setCustomerName(customer.getName());

        return paymentRepository.save(payment);
    }

    // Get all payments
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    // Get payment by MongoDB ID
    public Payment getPayment(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Payment not found with id: " + id));
    }

    // Update payment
    public Payment updatePayment(
            String id, PaymentRequestDTO paymentRequest) {

        Payment payment = getPayment(id);
        Order order = findOrderByBusinessId(paymentRequest.getOrderId());
        Customer customer = findCustomer(paymentRequest.getCustomerId());
        validateCustomerMatchesOrder(customer, order, paymentRequest.getCustomerName());

        if (nonBlank(paymentRequest.getPaymentId())) {
            payment.setPaymentId(paymentRequest.getPaymentId());
        } else if (!nonBlank(payment.getPaymentId())) {
            payment.setPaymentId("PAY-" + UUID.randomUUID());
        }
        payment.setOrderId(order.getOrderId());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus());
        payment.setPaymentType(paymentRequest.getPaymentType());
        payment.setPaymentDate(paymentRequest.getPaymentDate());
        payment.setPaymentTime(paymentRequest.getPaymentTime());
        payment.setPaymentAmount(paymentRequest.getPaymentAmount());
        payment.setCustomerId(customer.getId());
        payment.setCustomerName(customer.getName());

        return paymentRepository.save(payment);
    }

    // Delete payment
    public Boolean deletePayment(String id) {
        Payment payment = getPayment(id);
        paymentRepository.delete(payment);

        return true;
    }

    private Order findOrderByBusinessId(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order not found with orderId: " + orderId));
    }

    private Customer findCustomer(String customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
    }

    private void validateCustomerMatchesOrder(
            Customer customer, Order order, String requestedName) {
        if (!Objects.equals(customer.getName(), requestedName)
                || !Objects.equals(customer.getName(), order.getCustomer())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payment customer must match the order customer");
        }
    }

    private boolean nonBlank(String value) {
        return value != null && !value.isBlank();
    }
}
