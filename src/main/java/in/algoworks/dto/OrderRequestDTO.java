
package in.algoworks.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class OrderRequestDTO {

    private String orderId;

    @NotBlank(message = "Customer is required")
    private String customer;

    // Legacy fields: retain support for existing single-product requests.
    private String product;

    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;

    @Positive(message = "Order amount must be greater than zero")
    private Double amount;

    private String paymentStatus;

    @NotBlank(message = "Order status is required")
    private String orderStatus;

    private String date;

    // New fields for checkout orders.
    @Valid
    @NotEmpty(message = "At least one order item is required")
    private List<OrderItemRequestDTO> items;

    @Valid
    @NotNull(message = "Delivery address is required")
    private DeliveryAddressDTO deliveryAddress;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    public OrderRequestDTO() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<OrderItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequestDTO> items) {
        this.items = items;
    }

    public DeliveryAddressDTO getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(DeliveryAddressDTO deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public static class OrderItemRequestDTO {

        @NotBlank(message = "Product is required")
        private String product;

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        private Integer quantity;

        public OrderItemRequestDTO() {
        }

        public OrderItemRequestDTO(String product, Integer quantity) {
            this.product = product;
            this.quantity = quantity;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    public static class DeliveryAddressDTO {

        @NotBlank(message = "Full name is required")
        private String fullName;

        @NotBlank(message = "Phone is required")
        private String phone;

        @NotBlank(message = "Street is required")
        private String street;

        @NotBlank(message = "City is required")
        private String city;

        @NotBlank(message = "State is required")
        private String state;

        @NotBlank(message = "PIN code is required")
        private String pincode;

        public DeliveryAddressDTO() {
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getStreet() {
            return street;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }

        public String getPincode() {
            return pincode;
        }

        public void setPincode(String pincode) {
            this.pincode = pincode;
        }
    }
}
