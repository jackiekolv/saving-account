package com.example.savingAccount.service;

import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.exception.UnauthorizedException;
import com.example.savingAccount.repository.CustomerRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PinValidator {

    private final CustomerRepository customerRepository;

    public boolean validate(String citizenId, String plainPin) {
        Customer customer = customerRepository.findByCitizenId(citizenId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        if (!BCrypt.checkpw(plainPin, customer.getPin())) {
            throw new UnauthorizedException("Invalid PIN");
        }
        return true;
    }
}