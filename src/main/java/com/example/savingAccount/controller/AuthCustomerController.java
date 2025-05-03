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

@Validated
@RestController
@RequestMapping("/api/auth/customer")
@RequiredArgsConstructor
public class AuthCustomerController {

    private final CustomerService customerService;
    private final JwtUtil jwtUtil;

    // -------------------- Register --------------------
    @PostMapping("/register")
    public Customer register(@Valid @RequestBody Customer customer) {

        if(customerService.findByCitizenId(customer.getCitizenId()).isPresent()) throw new BadRequestException("This Citizen ID is already exist!");

        // hash password และ pin ก่อนเก็บ
        customer.setPassword(BCrypt.hashpw(customer.getPassword(), BCrypt.gensalt()));
        customer.setPin(BCrypt.hashpw(customer.getPlainPin(), BCrypt.gensalt()));
        return customerService.register(customer);
    }

    // -------------------- Login --------------------
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        Customer customer = customerService.findByCitizenId(request.getCitizenId())
                .orElseThrow(() -> new UnauthorizedException("Customer not found"));

        if (!BCrypt.checkpw(request.getPassword(), customer.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String jwt = jwtUtil.generateToken(customer.getCitizenId(), Role.CUSTOMER);
        System.out.println("jwt: " + jwt);
        return Map.of("token", jwt);
    }
}
