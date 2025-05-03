package com.example.savingAccount.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "The amount must be 1 THB or more")
    private BigDecimal amount;

    @NotBlank(message = "Channel is required")
    private String channel;
}