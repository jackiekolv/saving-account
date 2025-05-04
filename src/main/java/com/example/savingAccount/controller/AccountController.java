package com.example.savingAccount.controller;

import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.service.AccountService;
import com.example.savingAccount.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final CustomerService customerService;
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/create")
    public Account createAccount(@RequestParam String citizenId, @RequestParam BigDecimal deposit) {
        logger.info("Creating account for citizenId: {}", citizenId);
        Customer owner = customerService.findByCitizenId(citizenId)
                .orElseThrow(() -> {
                    String message = "Customer not found for citizenId: " + citizenId;
                    logger.warn(message);
                    throw new BadRequestException(message);
                });
        Account account = accountService.createAccount(owner, deposit);
        logger.info("Account created successfully for citizenId: {} with initial deposit: {}", citizenId, deposit);
        return account;
    }

    @PreAuthorize("hasRole('CUSTOMER') and #citizenId == authentication.name")
    @GetMapping("/info")
    public Customer accountInfo(@RequestParam String citizenId) {
        logger.info("Fetching account info for citizenId: {}", citizenId);
        return customerService.findByCitizenId(citizenId).orElseThrow();
    }
}
