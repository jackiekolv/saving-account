package com.example.savingAccount.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Citizen ID is required")
    private String citizenId;

    @NotBlank(message = "Password is required")
    private String password;
}