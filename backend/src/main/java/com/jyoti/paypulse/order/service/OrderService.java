package com.jyoti.paypulse.order.service;

import com.jyoti.paypulse.audit.service.AuditService;
import com.jyoti.paypulse.events.model.OrderPlacedEvent;
import com.jyoti.paypulse.events.publisher.EventPublisher;
import com.jyoti.paypulse.order.dto.CreateOrderRequest;
import com.jyoti.paypulse.order.dto.OrderResponse;
import com.jyoti.paypulse.order.model.Order;
import com.jyoti.paypulse.order.model.OrderStatus;
import com.jyoti.paypulse.order.model.Product;
import com.jyoti.paypulse.order.repository.OrderRepository;
import com.jyoti.paypulse.order.repository.ProductRepository;
import com.jyoti.paypulse.payment.dto.PaymentResponse;
import com.jyoti.paypulse.payment.dto.ProcessPaymentRequest;
import com.jyoti.paypulse.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private AuditService auditService;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public Optional<OrderResponse> getOrderById(Long id) {
        return orderRepository.findById(id).map(OrderResponse::fromEntity);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest req) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) correlationId = UUID.randomUUID().toString();

        // 1. Idempotency Check for Order creation
        if (req.getIdempotencyKey() != null && !req.getIdempotencyKey().trim().isEmpty()) {
            Optional<Order> existing = orderRepository.findByIdempotencyKey(req.getIdempotencyKey());
            if (existing.isPresent()) {
                log.warn(">>> [Order Service] Duplicate order detected for key: {}. Returning existing order.", req.getIdempotencyKey());
                auditService.record("OrderService", "IDEMPOTENT_ORDER_REPLAY",
                        "Idempotency hit: returned existing order #" + existing.get().getOrderNumber(), "WARN");
                return OrderResponse.fromEntity(existing.get());
            }
        }

        // 2. Fetch Product & Calculate Total
        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + req.getProductId()));

        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(req.getQuantity()));
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String idempotencyKey = (req.getIdempotencyKey() != null && !req.getIdempotencyKey().isEmpty())
                ? req.getIdempotencyKey()
                : UUID.randomUUID().toString();

        Order order = new Order(
                orderNumber,
                req.getCustomerEmail(),
                product.getId(),
                product.getName(),
                req.getQuantity(),
                totalAmount,
                idempotencyKey
        );
        Order savedOrder = orderRepository.save(order);

        auditService.record("OrderService", "ORDER_CREATED",
                String.format("Order #%s created for product '%s' (Qty: %d, Total: ₹%s) with status PENDING",
                        orderNumber, product.getName(), req.getQuantity(), totalAmount), "INFO");

        // 3. Publish Asynchronous Event
        eventPublisher.publish(new OrderPlacedEvent(
                savedOrder.getId(),
                orderNumber,
                req.getCustomerEmail(),
                totalAmount,
                idempotencyKey,
                correlationId
        ));

        // 4. Trigger Payment Processing
        ProcessPaymentRequest payReq = new ProcessPaymentRequest(
                savedOrder.getId(),
                req.getCustomerEmail(),
                totalAmount,
                idempotencyKey
        );

        PaymentResponse paymentRes = paymentService.processPayment(payReq);

        // 5. Update Order Status based on Payment Outcome
        if (paymentRes.success()) {
            savedOrder.setStatus(OrderStatus.CONFIRMED);
            savedOrder.setUpdatedAt(LocalDateTime.now());
            auditService.record("OrderService", "ORDER_CONFIRMED",
                    String.format("Order #%s confirmed successfully with Txn #%s", orderNumber, paymentRes.transactionId()), "INFO");
        } else {
            savedOrder.setStatus(OrderStatus.FAILED);
            savedOrder.setFailureReason(paymentRes.failureReason());
            savedOrder.setUpdatedAt(LocalDateTime.now());
            auditService.record("OrderService", "ORDER_FAILED",
                    String.format("Order #%s failed. Reason: %s", orderNumber, paymentRes.failureReason()), "ERROR");
        }

        Order finalOrder = orderRepository.save(savedOrder);
        return OrderResponse.fromEntity(finalOrder);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus status, String failureReason) {
        orderRepository.findById(orderId).ifPresent(order -> {
            order.setStatus(status);
            if (failureReason != null) {
                order.setFailureReason(failureReason);
            }
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
        });
    }
}
