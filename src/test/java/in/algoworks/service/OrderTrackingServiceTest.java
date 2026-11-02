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
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderTrackingServiceTest {

        private OrderRepository orderRepository;
        private CustomerRepository customerRepository;
        private ProductRepository productRepository;
        private OrderService orderService;

        @BeforeEach
        void setUp() {
                orderRepository = mock(OrderRepository.class);
                customerRepository = mock(CustomerRepository.class);
                productRepository = mock(ProductRepository.class);
                orderService = new OrderService(
                                orderRepository,
                                customerRepository,
                                productRepository,
                                mock(PaymentRepository.class));

                when(orderRepository.save(any(Order.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
        }

        private Order order(String status) {
                Order order = new Order();
                order.setId("o1");
                order.setOrderId("ORD-1");
                order.setCustomerId("cust-1");
                order.setOrderStatus(status);
                order.setDate("2026-10-01T10:00:00");
                order.setStockDeducted(true);
                order.setItems(new ArrayList<>(List.of(
                                new Order.OrderItem("p1", 2, 100.0))));
                when(orderRepository.findById("o1")).thenReturn(Optional.of(order));
                return order;
        }

        @Test
        void movesForwardAndRecordsTimeline() {
                order("Pending");

                Order updated = orderService.updateOrderStatus(
                                "o1", "confirmed", "Looks good", "admin-1");

                assertEquals("Confirmed", updated.getOrderStatus());
                // Seeded original status + the new one.
                assertEquals(2, updated.getStatusHistory().size());
                assertEquals("Pending", updated.getStatusHistory().get(0).getStatus());
                assertEquals("Confirmed", updated.getStatusHistory().get(1).getStatus());
                assertEquals("Looks good", updated.getStatusHistory().get(1).getNote());
                assertEquals("admin-1", updated.getStatusHistory().get(1).getUpdatedBy());
        }

        @Test
        void acceptsOutForDeliveryInSeveralSpellings() {
                order("Shipped");

                Order updated = orderService.updateOrderStatus(
                                "o1", "OUT_FOR_DELIVERY", null, "admin-1");

                assertEquals("Out for Delivery", updated.getOrderStatus());
        }

        @Test
        void rejectsBackwardAndInvalidTransitions() {
                order("Shipped");

                ResponseStatusException backward = assertThrows(
                                ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Pending", null, "a"));
                assertEquals(HttpStatus.CONFLICT, backward.getStatusCode());

                ResponseStatusException cancelAfterShip = assertThrows(
                                ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Cancelled", null, "a"));
                assertEquals(HttpStatus.CONFLICT, cancelAfterShip.getStatusCode());

                ResponseStatusException unknown = assertThrows(
                                ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Teleported", null, "a"));
                assertEquals(HttpStatus.BAD_REQUEST, unknown.getStatusCode());

                ResponseStatusException same = assertThrows(
                                ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Shipped", null, "a"));
                assertEquals(HttpStatus.BAD_REQUEST, same.getStatusCode());

                verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void deliveredOrdersAreFinal() {
                order("Delivered");

                assertThrows(ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Cancelled", null, "a"));
        }

        @Test
        void cancellingRestoresStockOnce() {
                order("Processing");
                Product product = new Product();
                product.setId("p1-id");
                when(productRepository.findById("p1")).thenReturn(Optional.of(product));

                Order updated = orderService.updateOrderStatus(
                                "o1", "Cancelled", "Customer request", "admin-1");

                assertEquals("Cancelled", updated.getOrderStatus());
                assertEquals(Boolean.FALSE, updated.getStockDeducted());
                verify(productRepository).incrementStockByQuantity("p1-id", 2);
        }

        @Test
        void legacyReturnedOrdersCannotChange() {
                order("Returned");

                assertThrows(ResponseStatusException.class,
                                () -> orderService.updateOrderStatus("o1", "Delivered", null, "a"));
        }

        @Test
        void customersCanOnlyReadTheirOwnOrders() {
                order("Pending");

                assertEquals("o1", orderService.getOrderForUser("o1", "cust-1", false).getId());
                assertEquals("o1", orderService.getOrderForUser("o1", "admin-1", true).getId());

                ResponseStatusException forbidden = assertThrows(
                                ResponseStatusException.class,
                                () -> orderService.getOrderForUser("o1", "someone-else", false));
                assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatusCode());
        }

        @Test
        void myOrdersAreSortedNewestFirst() {
                Order older = new Order();
                older.setDate("2026-10-01T10:00:00");
                Order newer = new Order();
                newer.setDate("2026-10-05T10:00:00");
                when(orderRepository.findByCustomerId("cust-1"))
                                .thenReturn(List.of(older, newer));

                List<Order> orders = orderService.getCustomerOrders("cust-1");

                assertTrue(orders.get(0) == newer);
        }

        @Test
        void checkoutSavesEnteredDeliveryNameInsteadOfRegisteredAccountName() {
                Customer account = new Customer();
                account.setId("cust-1");
                account.setName("Registered Account Name");
                account.setStatus("Active");
                when(customerRepository.findById("cust-1")).thenReturn(Optional.of(account));

                Product product = new Product();
                product.setId("prod-1");
                product.setName("Laptop");
                product.setPrice(50000.0);
                product.setStock(10);
                when(productRepository.findById("prod-1")).thenReturn(Optional.of(product));
                when(productRepository.decrementStockByQuantityIfAvailable("prod-1", 1, -1)).thenReturn(1L);
                when(orderRepository.findByOrderId(anyString())).thenReturn(Optional.empty());

                OrderRequestDTO request1 = new OrderRequestDTO();
                request1.setCustomer("Aarav Verma");
                request1.setItems(List.of(new OrderRequestDTO.OrderItemRequestDTO("prod-1", 1)));
                request1.setPaymentMethod("COD");
                request1.setOrderStatus("Pending");
                OrderRequestDTO.DeliveryAddressDTO address1 = new OrderRequestDTO.DeliveryAddressDTO();
                address1.setFullName("Aarav Verma");
                address1.setPhone("9876543210");
                address1.setStreet("12 MG Road");
                address1.setCity("Noida");
                address1.setState("Uttar Pradesh");
                address1.setPincode("201301");
                request1.setDeliveryAddress(address1);

                Order firstOrder = orderService.createOrder(request1, "cust-1");
                assertEquals("Aarav Verma", firstOrder.getCustomer());
                assertEquals("Aarav Verma", firstOrder.getDeliveryAddress().getFullName());
                assertEquals("cust-1", firstOrder.getCustomerId());

                OrderRequestDTO request2 = new OrderRequestDTO();
                request2.setCustomer("Priya Kapoor");
                request2.setItems(List.of(new OrderRequestDTO.OrderItemRequestDTO("prod-1", 1)));
                request2.setPaymentMethod("COD");
                request2.setOrderStatus("Pending");
                OrderRequestDTO.DeliveryAddressDTO address2 = new OrderRequestDTO.DeliveryAddressDTO();
                address2.setFullName("Priya Kapoor");
                address2.setPhone("9123456789");
                address2.setStreet("45 Park Street");
                address2.setCity("Delhi");
                address2.setState("Delhi");
                address2.setPincode("110001");
                request2.setDeliveryAddress(address2);

                Order secondOrder = orderService.createOrder(request2, "cust-1");
                assertEquals("Priya Kapoor", secondOrder.getCustomer());
                assertEquals("Priya Kapoor", secondOrder.getDeliveryAddress().getFullName());
                assertEquals("cust-1", secondOrder.getCustomerId());
        }
}
