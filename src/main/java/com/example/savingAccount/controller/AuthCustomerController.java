package com.example.savingAccount.controller;

import com.example.savingAccount.config.JwtUtil;
import com.example.savingAccount.dto.LoginRequest;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.entity.Role;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.exception.UnauthorizedException;
import com.example.savingAccount.service.CustomerService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Validated
@RestController
@RequestMapping("/api/auth/customer")
@RequiredArgsConstructor
public class AuthCustomerController {

    private static final Logger logger = LoggerFactory.getLogger(AuthCustomerController.class);

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    // -------------------- Register --------------------
    @PostMapping("/register")
    public Customer register(@Valid @RequestBody Customer customer) {
        logger.info("Registering customer with citizenId: {}", customer.getCitizenId());

        if(customerService.findByCitizenId(customer.getCitizenId()).isPresent()) {
            logger.info("Citizen ID already exists: {}", customer.getCitizenId());
            throw new BadRequestException("Citizen ID already exists: " + customer.getCitizenId());
        }

        // hash password และ pin ก่อนเก็บ
        customer.setPassword(BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt()));
        customer.setPin(BCrypt.hashpw(customer.getPlainPin(), BCrypt.gensalt()));
        logger.info("Customer registered successfully: {}", customer.getCitizenId());
        return customerService.register(customer);
    }

    // -------------------- Login --------------------
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        logger.info("Login attempt for citizenId: {}", request.getCitizenId());
        Customer customer = customerService.findByCitizenId(request.getCitizenId())
                .orElseThrow(() -> {
                    logger.warn("Login failed - customer not found: {}", request.getCitizenId());
                    throw new UnauthorizedException("Login failed - customer not found: " + request.getCitizenId());
                });

        if (!BCrypt.checkpw(request.getPassword(), customer.getPassword())) {
            logger.warn("Login failed - invalid password for citizenId: {}", request.getCitizenId());
            throw new UnauthorizedException("Login failed - invalid password for citizenId: " + request.getCitizenId());
        }

        String jwt = jwtUtil.generateToken(customer.getCitizenId(), Role.CUSTOMER);
        logger.info("Login successful for citizenId: {}", request.getCitizenId());
        System.out.println("jwt: " + jwt);
        return Map.of("token", jwt);
    }
}
