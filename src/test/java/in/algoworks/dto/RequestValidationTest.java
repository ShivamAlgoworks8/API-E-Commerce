package in.algoworks.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private final Validator validator =
            Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void allowsServerCalculatedOrderAmountButRejectsNonPositiveSuppliedAmount() {
        OrderRequestDTO request = new OrderRequestDTO();
        request.setCustomer("customer-id");
        request.setProduct("product-id");
        request.setOrderStatus("Processing");

        assertTrue(validator.validate(request).isEmpty());

        request.setAmount(0.0);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void allowsServerCalculatedPaymentAmountAndOmittedCustomerName() {
        PaymentRequestDTO request = new PaymentRequestDTO();
        request.setOrderId("ORD-1001");
        request.setPaymentStatus("Paid");
        request.setPaymentType("Card");
        request.setCustomerId("customer-id");

        assertTrue(validator.validate(request).isEmpty());

        request.setPaymentAmount(0.0);
        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void allowsMerchantRequestWithoutBrandName() {
        MerchantRequestDTO request = new MerchantRequestDTO();
        request.setMerchantName("A Merchant");
        request.setProductType("Electronics");
        request.setStatus("Active");

        assertTrue(validator.validate(request).isEmpty());
    }
}
