package com.jyoti.paypulse.payment.controller;

import com.jyoti.paypulse.payment.dto.ChaosStatusResponse;
import com.jyoti.paypulse.payment.dto.PaymentResponse;
import com.jyoti.paypulse.payment.dto.ProcessPaymentRequest;
import com.jyoti.paypulse.payment.dto.WalletTopupRequest;
import com.jyoti.paypulse.payment.model.PaymentTransaction;
import com.jyoti.paypulse.payment.model.Wallet;
import com.jyoti.paypulse.payment.repository.PaymentTransactionRepository;
import com.jyoti.paypulse.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Payments & Wallet", description = "Idempotent payment processing, Resilience4j circuit breakers, and chaos toggles")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @GetMapping("/wallet")
    @Operation(summary = "Get user wallet balance and details")
    public ResponseEntity<Wallet> getWallet(@RequestParam(defaultValue = "jyoti@paypulse.io") String email) {
        return ResponseEntity.ok(paymentService.getOrCreateWallet(email));
    }

    @PostMapping("/wallet/topup")
    @Operation(summary = "Add funds to user wallet balance")
    public ResponseEntity<Wallet> topupWallet(@Valid @RequestBody WalletTopupRequest req) {
        return ResponseEntity.ok(paymentService.topupWallet(req.getUserEmail(), req.getAmount()));
    }

    @PostMapping("/payments/process")
    @Operation(summary = "Process idempotent order payment with Resilience4j Circuit Breaker protection")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody ProcessPaymentRequest req) {
        return ResponseEntity.ok(paymentService.processPayment(req));
    }

    @GetMapping("/payments/transactions")
    @Operation(summary = "Get all payment transactions with status and idempotency audit")
    public ResponseEntity<List<PaymentTransaction>> getTransactions() {
        return ResponseEntity.ok(paymentTransactionRepository.findAllByOrderByCreatedAtDesc());
    }

    @GetMapping("/chaos/status")
    @Operation(summary = "Get current Circuit Breaker state and chaos simulation status")
    public ResponseEntity<ChaosStatusResponse> getChaosStatus() {
        return ResponseEntity.ok(paymentService.getChaosStatus());
    }

    @PostMapping("/chaos/toggle")
    @Operation(summary = "Toggle simulated payment gateway failure to demonstrate Circuit Breaker tripping")
    public ResponseEntity<ChaosStatusResponse> toggleChaos(@RequestBody Map<String, Boolean> body) {
        boolean enable = body.getOrDefault("enable", false);
        return ResponseEntity.ok(paymentService.toggleChaos(enable));
    }
}
