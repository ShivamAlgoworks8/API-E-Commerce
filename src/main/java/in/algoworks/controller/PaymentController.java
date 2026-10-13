
package in.algoworks.controller;

import in.algoworks.entity.Payment;
import in.algoworks.service.PaymentService;
import org.springframework.web.bind.annotation.*;
import in.algoworks.dto.PaymentRequestDTO;
import java.util.List;
@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create payment
    @PostMapping
    public Payment createPayment(@RequestBody PaymentRequestDTO paymentRequest) {
        return paymentService.createPayment(paymentRequest);
    }


    // Get all payments
    @GetMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }

    // Get payment by ID
    @GetMapping("/{id}")
    public Payment getPayment(@PathVariable String id) {
        return paymentService.getPayment(id);
    }

    // Update payment
    @PutMapping("/{id}")
    public Payment updatePayment(
            @PathVariable String id,
            @RequestBody PaymentRequestDTO paymentRequest) {
        return paymentService.updatePayment(id, paymentRequest);
    }

    // Delete payment
    @DeleteMapping("/{id}")
    public Boolean deletePayment(@PathVariable String id) {
        return paymentService.deletePayment(id);
    }
}
