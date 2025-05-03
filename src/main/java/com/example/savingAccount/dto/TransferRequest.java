package com.example.savingAccount.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(exclude = {"pin", "message"})
public class TransferRequest {

    private String message;

    @NotBlank(message = "From account is required")
    private String accountNumberFrom;

    @NotBlank(message = "To account is required")
    private String accountNumberTo;

    @NotNull(message = "Amount is required")
    private BigDecimal amount;

    @NotBlank(message = "PIN is required", groups = ConfirmGroup.class)
    @Pattern(regexp = "\\d{6}", message = "PIN must be 6 digits")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String pin;

    @NotBlank(message = "Channel is required")
    private String channel;

}