package in.algoworks.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// This class represents an Order document in MongoDB
@Document(collection = "orders")
public class Order {

    // MongoDB automatically uses this field as the unique ID
    @Id
    private String id;

    // Order's display/business ID, for example: ORD-1001
    private String orderId;

    // Customer who placed the order
    private String customer;

    // Product included in the order
    private String product;

    // Total amount of the order
    private double amount;

    // Payment status: Paid, Pending, or Failed
    private String paymentStatus;

    // Order status: Delivered, Processing, Cancelled, or Returned
    private String orderStatus;

    // Date on which the order was placed
    private String date;

    // Default constructor required by Spring/MongoDB
    public Order() {
    }

    // Get MongoDB ID
    public String getId() {
        return id;
    }

    // Set MongoDB ID
    public void setId(String id) {
        this.id = id;
    }

    // Get order ID
    public String getOrderId() {
        return orderId;
    }

    // Set order ID
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    // Get customer name
    public String getCustomer() {
        return customer;
    }

    // Set customer name
    public void setCustomer(String customer) {
        this.customer = customer;
    }

    // Get product name
    public String getProduct() {
        return product;
    }

    // Set product name
    public void setProduct(String product) {
        this.product = product;
    }

    // Get order amount
    public double getAmount() {
        return amount;
    }

    // Set order amount
    public void setAmount(double amount) {
        this.amount = amount;
    }

    // Get payment status
    public String getPaymentStatus() {
        return paymentStatus;
    }

    // Set payment status
    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    // Get order status
    public String getOrderStatus() {
        return orderStatus;
    }

    // Set order status
    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    // Get order date
    public String getDate() {
        return date;
    }

    // Set order date
    public void setDate(String date) {
        this.date = date;
    }
}