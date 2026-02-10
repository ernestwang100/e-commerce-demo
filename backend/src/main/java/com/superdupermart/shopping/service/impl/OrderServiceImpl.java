package com.superdupermart.shopping.service.impl;

import com.superdupermart.shopping.dao.OrderDao;
import com.superdupermart.shopping.dao.ProductDao;
import com.superdupermart.shopping.dao.UserDao;
import com.superdupermart.shopping.dto.OrderItemRequest;
import com.superdupermart.shopping.dto.OrderItemResponse;
import com.superdupermart.shopping.dto.OrderRequest;
import com.superdupermart.shopping.dto.OrderResponse;
import com.superdupermart.shopping.dto.PageResponse; // Import
import com.superdupermart.shopping.entity.Order;
import com.superdupermart.shopping.entity.OrderItem;
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

        @Autowired
        public OrderServiceImpl(OrderDao orderDao, ProductDao productDao, UserDao userDao,
                        KafkaTemplate<String, String> kafkaTemplate, EmailService emailService,
                        PaymentService paymentService) {
                this.orderDao = orderDao;
                this.productDao = productDao;
                this.userDao = userDao;
                this.kafkaTemplate = kafkaTemplate;
                this.emailService = emailService;
                this.paymentService = paymentService;
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
                                .build();

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

                return OrderResponse.builder()
                                .orderId(order.getId())
                                .datePlaced(order.getDatePlaced())
                                .orderStatus(order.getOrderStatus())
                                .items(itemResponses)
                                .build();
        }
}
