package com.jyoti.paypulse;

import com.jyoti.paypulse.audit.service.AuditService;
import com.jyoti.paypulse.events.publisher.EventPublisher;
import com.jyoti.paypulse.payment.dto.PaymentResponse;
import com.jyoti.paypulse.payment.dto.ProcessPaymentRequest;
import com.jyoti.paypulse.payment.model.PaymentTransaction;
import com.jyoti.paypulse.payment.model.Wallet;
import com.jyoti.paypulse.payment.repository.PaymentTransactionRepository;
import com.jyoti.paypulse.payment.repository.WalletRepository;
import com.jyoti.paypulse.payment.service.PaymentService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTests {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PaymentTransactionRepository paymentTransactionRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private AuditService auditService;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @InjectMocks
    private PaymentService paymentService;

    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        mockWallet = new Wallet("test@paypulse.io", new BigDecimal("5000.00"));
    }

    @Test
    void testIdempotencyReturnsPreviousTransactionWithoutDeduction() {
        String key = "IDEMP-KEY-12345";
        PaymentTransaction existingTxn = new PaymentTransaction(
                "TXN-EXISTING-1", 101L, key, new BigDecimal("1000.00"), "SUCCESS", "REF-1", null
        );

        when(paymentTransactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existingTxn));

        ProcessPaymentRequest req = new ProcessPaymentRequest(101L, "test@paypulse.io", new BigDecimal("1000.00"), key);
        PaymentResponse response = paymentService.processPayment(req);

        assertNotNull(response);
        assertTrue(response.idempotentReplay());
        assertEquals("TXN-EXISTING-1", response.transactionId());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testInsufficientWalletBalanceFailsTransaction() {
        String key = "IDEMP-KEY-67890";
        mockWallet.setBalance(new BigDecimal("100.00"));

        when(paymentTransactionRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        when(walletRepository.findByUserEmail("test@paypulse.io")).thenReturn(Optional.of(mockWallet));

        ProcessPaymentRequest req = new ProcessPaymentRequest(102L, "test@paypulse.io", new BigDecimal("2000.00"), key);
        PaymentResponse response = paymentService.processPayment(req);

        assertNotNull(response);
        assertFalse(response.success());
        assertEquals("FAILED", response.status());
        assertTrue(response.failureReason().contains("Insufficient wallet balance"));
        assertEquals(new BigDecimal("100.00"), mockWallet.getBalance()); // balance unchanged
    }
}
