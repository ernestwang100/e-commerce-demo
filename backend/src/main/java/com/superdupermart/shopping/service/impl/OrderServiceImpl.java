package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.AddressDao;
import com.superdupermart.shopping.dao.OrderDao;
import com.superdupermart.shopping.dao.PaymentMethodDao;
import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dto.OrderItemRequest;
import com.superdupermart.shopping.dto.OrderItemResponse;
import com.superdupermart.shopping.dto.OrderRequest;
import com.superdupermart.shopping.dto.OrderResponse;
import com.superdupermart.shopping.dto.PageResponse; // Import
import com.superdupermart.shopping.entity.Address;
import com.superdupermart.shopping.entity.Order;
import com.superdupermart.shopping.entity.OrderItem;
import com.superdupermart.shopping.entity.PaymentMethod;
import com.superdupermart.shopping.entity.Product;
import com.superdupermart.shopping.entity.User;
import com.superdupermart.shopping.exception.NotEnoughInventoryException;
import com.superdupermart.shopping.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.kafka.core.KafkaTemplate;
import com.superdupermart.shopping.service.EmailService;
import com.superdupermart.shopping.service.PaymentService;

@Service
public class OrderServiceImpl implements OrderService {

        private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OrderServiceImpl.class);

        private final OrderDao orderDao;
        private final ProductDao productDao;
        private final UserDao userDao;
        private final KafkaTemplate<String, String> kafkaTemplate;
        private final EmailService emailService;
        private final PaymentService paymentService;
        private final AddressDao addressDao;
        private final PaymentMethodDao paymentMethodDao;

        @Autowired
        public OrderServiceImpl(OrderDao orderDao, ProductDao productDao, UserDao userDao,
                        KafkaTemplate<String, String> kafkaTemplate, EmailService emailService,
                        PaymentService paymentService, AddressDao addressDao, PaymentMethodDao paymentMethodDao) {
                this.orderDao = orderDao;
                this.productDao = productDao;
                this.userDao = userDao;
                this.kafkaTemplate = kafkaTemplate;
                this.emailService = emailService;
                this.paymentService = paymentService;
                this.addressDao = addressDao;
                this.paymentMethodDao = paymentMethodDao;
        }

        @Override
        @Transactional
        public OrderResponse placeOrder(Integer userId, OrderRequest request) {
                User user = userDao.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));

                Order order = Order.builder()
                                .user(user)
                                .datePlaced(LocalDateTime.now())
                                .orderStatus("Processing")
                                .items(new ArrayList<>())
                                .isPickup(request.getIsPickup() != null ? request.getIsPickup() : false)
                                .build();

                // Handle Shipping Address
                if (!order.getIsPickup()) {
                        if (request.getAddressId() != null) {
                                Address address = addressDao.findById(request.getAddressId())
                                                .orElseThrow(() -> new RuntimeException("Address not found"));
                                order.setShippingAddress(address);
                        } else if (request.getNewAddress() != null) {
                                Address newAddress = request.getNewAddress();
                                newAddress.setUser(user);
                                addressDao.save(newAddress);
                                order.setShippingAddress(newAddress);
                        } else {
                                throw new RuntimeException("Shipping address is required for delivery");
                        }
                }

                // Handle Payment Method
                if (request.getPaymentMethodId() != null) {
                        PaymentMethod paymentMethod = paymentMethodDao.findById(request.getPaymentMethodId())
                                        .orElseThrow(() -> new RuntimeException("Payment method not found"));
                        order.setPaymentMethod(paymentMethod);
                } else if (request.getNewPaymentMethod() != null) {
                        PaymentMethod newPaymentMethod = request.getNewPaymentMethod();
                        newPaymentMethod.setUser(user);
                        paymentMethodDao.save(newPaymentMethod);
                        order.setPaymentMethod(newPaymentMethod);
                } else {
                        // For now allow null payment method if not provided (maybe generic payment)
                        // logic for standard checkout
                }

                double totalAmount = 0.0;

                for (OrderItemRequest itemRequest : request.getOrder()) {
                        Product product = productDao.findById(itemRequest.getProductId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Product not found: " + itemRequest.getProductId()));

                        if (product.getQuantity() < itemRequest.getQuantity()) {
                                throw new NotEnoughInventoryException(
                                                "Not enough inventory for product: " + product.getName());
                        }

                        product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
                        productDao.update(product);

                        OrderItem orderItem = OrderItem.builder()
                                        .order(order)
                                        .product(product)
                                        .quantity(itemRequest.getQuantity())
                                        .purchasedPrice(product.getRetailPrice())
                                        .build();

                        order.getItems().add(orderItem);
                        totalAmount += product.getRetailPrice()
                                        .multiply(java.math.BigDecimal.valueOf(itemRequest.getQuantity()))
                                        .doubleValue();
                }

                // Simulate payment authorization
                paymentService.authorizeTransaction(totalAmount);

                orderDao.save(order);

                // Publish event to Kafka
                String message = "Order placed successfully. Order ID: " + order.getId() + ", User: "
                                + user.getUsername();
                kafkaTemplate.send("orders", message);
                log.info("Published event to Kafka: {}", message);

                // Send async confirmation email
                emailService.sendOrderConfirmation(user.getEmail(), order.getId());

                return mapToResponse(order);
        }

        @Override
        @Transactional
        public void cancelOrder(Integer orderId, Integer userId, boolean isAdmin) {
                Order order = orderDao.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                if (!isAdmin && !order.getUser().getId().equals(userId)) {
                        throw new RuntimeException("Unauthorized to cancel this order");
                }

                if (!"Processing".equals(order.getOrderStatus())) {
                        throw new RuntimeException("Only 'Processing' orders can be canceled");
                }

                for (OrderItem item : order.getItems()) {
                        Product product = item.getProduct();
                        product.setQuantity(product.getQuantity() + item.getQuantity());
                        productDao.update(product);
                }

                order.setOrderStatus("Canceled");
                orderDao.update(order);
        }

        @Override
        @Transactional
        public void completeOrder(Integer orderId) {
                Order order = orderDao.findById(orderId)
                                .orElseThrow(() -> new RuntimeException("Order not found"));

                if (!"Processing".equals(order.getOrderStatus())) {
                        throw new RuntimeException("Only 'Processing' orders can be completed");
                }

                order.setOrderStatus("Completed");
                orderDao.update(order);
        }

        @Override
        public List<OrderResponse> getOrdersByUser(Integer userId) {
                User user = userDao.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                return orderDao.getOrdersByUser(user).stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        public PageResponse<OrderResponse> getOrdersPage(int page, int size) {
                List<Order> orders = orderDao.getPaginatedOrders(page, size);
                long totalElements = orderDao.countOrders();
                int totalPages = (int) Math.ceil((double) totalElements / size);

                List<OrderResponse> content = orders.stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());

                return PageResponse.<OrderResponse>builder()
                                .content(content)
                                .totalElements(totalElements)
                                .totalPages(totalPages)
                                .size(size)
                                .number(page)
                                .build();
        }

        private OrderResponse mapToResponse(Order order) {
                List<OrderItemResponse> itemResponses = order.getItems().stream()
                                .map(item -> OrderItemResponse.builder()
                                                .productId(item.getProduct().getId())
                                                .productName(item.getProduct().getName())
                                                .quantity(item.getQuantity())
                                                .purchasedPrice(item.getPurchasedPrice())
                                                .build())
                                .collect(Collectors.toList());

                OrderResponse.AddressInfo addressInfo = null;
                if (Boolean.TRUE.equals(order.getIsPickup()) == false && order.getShippingAddress() != null) {
                        Address addr = order.getShippingAddress();
                        addressInfo = OrderResponse.AddressInfo.builder()
                                        .fullName(addr.getFullName())
                                        .addressLine1(addr.getAddressLine1())
                                        .addressLine2(addr.getAddressLine2())
                                        .city(addr.getCity())
                                        .state(addr.getState())
                                        .zipCode(addr.getZipCode())
                                        .country(addr.getCountry())
                                        .build();
                }

                OrderResponse.PaymentInfo paymentInfo = null;
                if (order.getPaymentMethod() != null) {
                        paymentInfo = OrderResponse.PaymentInfo.builder()
                                        .cardType(order.getPaymentMethod().getCardType())
                                        .last4Digits(order.getPaymentMethod().getLast4())
                                        .build();
                }

                return OrderResponse.builder()
                                .orderId(order.getId())
                                .datePlaced(order.getDatePlaced())
                                .orderStatus(order.getOrderStatus())
                                .items(itemResponses)
                                .userId(order.getUser().getId())
                                .customerUsername(order.getUser().getUsername())
                                .customerEmail(order.getUser().getEmail())
                                .isPickup(Boolean.TRUE.equals(order.getIsPickup()))
                                .shippingAddress(addressInfo)
                                .paymentMethod(paymentInfo)
                                .build();
        }
}
