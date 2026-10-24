package com.jyoti.paypulse.payment.service;

import com.jyoti.paypulse.audit.service.AuditService;
import com.jyoti.paypulse.events.model.PaymentProcessedEvent;
import com.jyoti.paypulse.events.publisher.EventPublisher;
import com.jyoti.paypulse.payment.dto.ChaosStatusResponse;
import com.jyoti.paypulse.payment.dto.PaymentResponse;
import com.jyoti.paypulse.payment.dto.ProcessPaymentRequest;
import com.jyoti.paypulse.payment.model.PaymentTransaction;
import com.jyoti.paypulse.payment.model.Wallet;
import com.jyoti.paypulse.payment.repository.PaymentTransactionRepository;
import com.jyoti.paypulse.payment.repository.WalletRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private AuditService auditService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    // Chaos toggle for live interview demonstration
    private final AtomicBoolean simulatedGatewayDown = new AtomicBoolean(false);

    @Transactional
    public Wallet getOrCreateWallet(String userEmail) {
        return walletRepository.findByUserEmail(userEmail)
                .orElseGet(() -> {
                    Wallet newWallet = new Wallet(userEmail, new BigDecimal("5000.00")); // Seed with ₹5,000 for evaluation
                    return walletRepository.save(newWallet);
                });
    }

    @Transactional
    public Wallet topupWallet(String userEmail, BigDecimal amount) {
        Wallet wallet = getOrCreateWallet(userEmail);
        wallet.setBalance(wallet.getBalance().add(amount));
        wallet.setUpdatedAt(LocalDateTime.now());
        Wallet saved = walletRepository.save(wallet);

        auditService.record("PaymentService", "WALLET_TOPUP",
                String.format("Added ₹%s to wallet for %s. New balance: ₹%s", amount, userEmail, saved.getBalance()), "INFO");
        return saved;
    }

    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest req) {
        String correlationId = MDC.get("correlationId");
        if (correlationId == null) correlationId = UUID.randomUUID().toString();

        // 1. Idempotency Check: Guard against duplicate requests
        Optional<PaymentTransaction> existing = paymentTransactionRepository.findByIdempotencyKey(req.getIdempotencyKey());
        if (existing.isPresent()) {
            PaymentTransaction prev = existing.get();
            log.warn(">>> [Idempotency Guard] Duplicate request detected for key: {}. Returning existing record.", req.getIdempotencyKey());
            auditService.record("PaymentService", "IDEMPOTENT_REPLAY",
                    String.format("Blocked duplicate charge for order #%d with key: %s", req.getOrderId(), req.getIdempotencyKey()), "WARN");

            return new PaymentResponse(
                    prev.getTransactionId(),
                    prev.getOrderId(),
                    prev.getIdempotencyKey(),
                    prev.getAmount(),
                    "SUCCESS".equals(prev.getStatus()),
                    prev.getStatus(),
                    prev.getFailureReason(),
                    getCircuitBreakerState(),
                    true, // idempotentReplay
                    prev.getCreatedAt()
            );
        }

        // 2. Wallet Balance Verification
        Wallet wallet = getOrCreateWallet(req.getUserEmail());
        if (wallet.getBalance().compareTo(req.getAmount()) < 0) {
            String txnId = "TXN-FAIL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            PaymentTransaction failedTxn = new PaymentTransaction(
                    txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                    "FAILED", null, "Insufficient wallet balance (Available: ₹" + wallet.getBalance() + ")"
            );
            paymentTransactionRepository.save(failedTxn);

            auditService.record("PaymentService", "PAYMENT_FAILED",
                    String.format("Insufficient balance for order #%d (Req: ₹%s, Avail: ₹%s)", req.getOrderId(), req.getAmount(), wallet.getBalance()), "WARN");

            eventPublisher.publish(new PaymentProcessedEvent(req.getOrderId(), txnId, req.getAmount(), false, failedTxn.getFailureReason(), getCircuitBreakerState(), correlationId));

            return new PaymentResponse(
                    txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                    false, "FAILED", failedTxn.getFailureReason(), getCircuitBreakerState(), false, LocalDateTime.now()
            );
        }

        // 3. Process payment through resilient gateway call (with Circuit Breaker)
        try {
            return callExternalGatewayWithCircuitBreaker(wallet, req, correlationId);
        } catch (Exception ex) {
            log.error("Payment gateway error: {}", ex.getMessage());
            return executeGatewayFallback(wallet, req, correlationId, ex);
        }
    }

    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "paymentGateway", fallbackMethod = "executeGatewayFallback")
    public PaymentResponse callExternalGatewayWithCircuitBreaker(Wallet wallet, ProcessPaymentRequest req, String correlationId) {
        // Chaos simulation check
        if (simulatedGatewayDown.get()) {
            auditService.record("PaymentGateway", "SIMULATED_FAILURE",
                    "Simulated external gateway outage triggered. Throwing RuntimeException.", "ERROR");
            throw new RuntimeException("External Payment Gateway is experiencing 503 Outage (Chaos Simulated)");
        }

        // Deduct wallet balance
        wallet.setBalance(wallet.getBalance().subtract(req.getAmount()));
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);

        // Record successful transaction
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String providerRef = "PAYPULSE-REF-" + System.currentTimeMillis();
        PaymentTransaction txn = new PaymentTransaction(
                txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                "SUCCESS", providerRef, null
        );
        paymentTransactionRepository.save(txn);

        auditService.record("PaymentService", "PAYMENT_SUCCESS",
                String.format("Payment of ₹%s processed for order #%d. Txn: %s. New balance: ₹%s", req.getAmount(), req.getOrderId(), txnId, wallet.getBalance()), "INFO");

        // Emit asynchronous event
        eventPublisher.publish(new PaymentProcessedEvent(req.getOrderId(), txnId, req.getAmount(), true, null, getCircuitBreakerState(), correlationId));

        return new PaymentResponse(
                txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                true, "SUCCESS", null, getCircuitBreakerState(), false, LocalDateTime.now()
        );
    }

    // Graceful Fallback when Circuit Breaker is OPEN or throws exception
    public PaymentResponse executeGatewayFallback(Wallet wallet, ProcessPaymentRequest req, String correlationId, Throwable t) {
        String txnId = "TXN-FALLBACK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String cbState = getCircuitBreakerState();

        PaymentTransaction fallbackTxn = new PaymentTransaction(
                txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                "FALLBACK_CIRCUIT_OPEN", null, "Circuit Breaker " + cbState + ": " + t.getMessage()
        );
        paymentTransactionRepository.save(fallbackTxn);

        auditService.record("Resilience4j", "CIRCUIT_BREAKER_FALLBACK",
                String.format("Circuit Breaker [%s] intercepted failure for order #%d: %s", cbState, req.getOrderId(), t.getMessage()), "WARN");

        // Notify order service that payment could not be processed
        eventPublisher.publish(new PaymentProcessedEvent(req.getOrderId(), txnId, req.getAmount(), false, fallbackTxn.getFailureReason(), cbState, correlationId));

        return new PaymentResponse(
                txnId, req.getOrderId(), req.getIdempotencyKey(), req.getAmount(),
                false, "FALLBACK_CIRCUIT_OPEN", fallbackTxn.getFailureReason(), cbState, false, LocalDateTime.now()
        );
    }

    public String getCircuitBreakerState() {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");
            return cb.getState().name();
        } catch (Exception e) {
            return "CLOSED";
        }
    }

    public void resetCircuitBreaker() {
        try {
            CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("paymentGateway");
            cb.reset();
        } catch (Exception ignored) {}
    }

    public ChaosStatusResponse toggleChaos(boolean enable) {
        simulatedGatewayDown.set(enable);
        if (!enable) {
            resetCircuitBreaker();
        }

        String state = getCircuitBreakerState();
        String message = enable
                ? "Simulated Gateway Outage ENABLED. Next requests will trigger Circuit Breaker trip."
                : "Simulated Gateway Outage DISABLED. Circuit Breaker reset to CLOSED.";

        auditService.record("ChaosEngineering", "CHAOS_TOGGLE", message, enable ? "WARN" : "INFO");
        return new ChaosStatusResponse(enable, state, 50, message);
    }

    public ChaosStatusResponse getChaosStatus() {
        return new ChaosStatusResponse(
                simulatedGatewayDown.get(),
                getCircuitBreakerState(),
                50,
                simulatedGatewayDown.get() ? "Gateway simulated DOWN" : "Gateway operating normally"
        );
    }
}
