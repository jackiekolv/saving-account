package com.example.savingAccount.service;


import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.exception.UnauthorizedException;
import com.example.savingAccount.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class PinValidatorTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private PinValidator pinValidator;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void validate_shouldSucceedWithCorrectPin() {
        String citizenId = "1234567890123";
        String plainPin = "123456";
        String hashedPin = BCrypt.hashpw(plainPin, BCrypt.gensalt());

        Customer customer = new Customer();
        customer.setCitizenId(citizenId);
        customer.setPin(hashedPin);

        when(customerRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(customer));

        pinValidator.validate(citizenId, plainPin); // Should not throw
    }

    @Test
    void validate_shouldThrowIfCustomerNotFound() {
        String citizenId = "0000000000000";

        when(customerRepository.findByCitizenId(citizenId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> pinValidator.validate(citizenId, "123456"));
        assertEquals("Customer not found", exception.getMessage());
    }

    @Test
    void validate_shouldThrowIfPinInvalid() {
        String citizenId = "1234567890123";
        String plainPin = "123456";
        String wrongPin = "000000";
        String hashedPin = BCrypt.hashpw(plainPin, BCrypt.gensalt());

        Customer customer = new Customer();
        customer.setCitizenId(citizenId);
        customer.setPin(hashedPin);

        when(customerRepository.findByCitizenId(citizenId)).thenReturn(Optional.of(customer));

        UnauthorizedException exception = assertThrows(UnauthorizedException.class, () -> pinValidator.validate(citizenId, wrongPin));
        assertEquals("Invalid PIN", exception.getMessage());
    }
}