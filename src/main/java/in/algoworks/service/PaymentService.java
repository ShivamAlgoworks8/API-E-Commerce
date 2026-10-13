package in.algoworks.service;
import in.algoworks.entity.Payment;
import in.algoworks.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import in.algoworks.dto.PaymentRequestDTO;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {
    //Repo used to perform final operations

    private final PaymentRepository paymentRepository;

    //constructor injection Spring provides the PaymentRepository object

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    // Create a new payment and save it to MongoDB
    public Payment createPayment(PaymentRequestDTO paymentRequest) {

        Payment payment = new Payment();

        payment.setPaymentId(paymentRequest.getPaymentId());
        payment.setOrderId(paymentRequest.getOrderId());
        payment.setPaymentStatus(paymentRequest.getPaymentStatus());
        payment.setPaymentType(paymentRequest.getPaymentType());
        payment.setPaymentDate(paymentRequest.getPaymentDate());
        payment.setPaymentTime(paymentRequest.getPaymentTime());
        payment.setPaymentAmount(paymentRequest.getPaymentAmount());
        payment.setCustomerId(paymentRequest.getCustomerId());
        payment.setCustomerName(paymentRequest.getCustomerName());

        return paymentRepository.save(payment);
    }


    // Get all payments
    public List<Payment> getAllPayments() {

        // Fetch all payment from MongoDB
        return paymentRepository.findAll();
    }

    public Payment getPayment(String id) {
        return paymentRepository.findById(id).orElse(null);
    }

    public Payment updatePayment(String id, PaymentRequestDTO paymentRequest) {

        Optional<Payment> existingPayment = paymentRepository.findById(id);
        if (existingPayment.isEmpty()) {
            return null;
        }
        Payment paymentToUpdate = existingPayment.get();
        paymentToUpdate.setPaymentId(paymentRequest.getPaymentId());


        paymentToUpdate.setOrderId(paymentRequest.getOrderId());
        paymentToUpdate.setPaymentStatus(paymentRequest.getPaymentStatus());
        paymentToUpdate.setPaymentType(paymentRequest.getPaymentType());
        paymentToUpdate.setPaymentDate(paymentRequest.getPaymentDate());
        paymentToUpdate.setPaymentTime(paymentRequest.getPaymentTime());
        paymentToUpdate.setPaymentAmount(paymentRequest.getPaymentAmount());
        paymentToUpdate.setCustomerId(paymentRequest.getCustomerId());
        paymentToUpdate.setCustomerName(paymentRequest.getCustomerName());
        return paymentRepository.save(paymentToUpdate);

    }
    public Boolean deletePayment(String id) {
        Boolean exists = paymentRepository.existsById(id);

        if (!exists) {
            return false;
        }

        paymentRepository.deleteById(id);
        return true;
    }

    }






