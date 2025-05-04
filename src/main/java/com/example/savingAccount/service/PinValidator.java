package com.example.savingAccount.service;

import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.exception.UnauthorizedException;
import com.example.savingAccount.repository.CustomerRepository;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PinValidator {

    private static final Logger log = LoggerFactory.getLogger(PinValidator.class);

    private final CustomerRepository customerRepository;

    public boolean validate(String citizenId, String plainPin) {
        log.info("Validating PIN for citizenId: {}", citizenId);
        Customer customer = customerRepository.findByCitizenId(citizenId)
                .orElseThrow(() -> {
                    log.warn("Customer not found for citizenId: {}", citizenId);
                    return new NotFoundException("Customer not found");
                });

        if (!BCrypt.checkpw(plainPin, customer.getPin())) {
            log.warn("PIN validation failed for citizenId: {}", citizenId);
            throw new UnauthorizedException("Invalid PIN");
        }
        log.info("PIN validated successfully for citizenId: {}", citizenId);
        return true;
    }
}