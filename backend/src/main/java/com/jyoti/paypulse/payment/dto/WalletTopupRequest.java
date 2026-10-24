package com.jyoti.paypulse.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WalletTopupRequest {

    @NotBlank(message = "User email is required")
    private String userEmail;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Minimum topup amount is ₹1.00")
    private BigDecimal amount;

    public WalletTopupRequest() {}

    public WalletTopupRequest(String userEmail, BigDecimal amount) {
        this.userEmail = userEmail;
        this.amount = amount;
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
}
