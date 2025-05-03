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

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final CustomerService customerService;

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/create")
    public Account createAccount(@RequestParam String citizenId, @RequestParam BigDecimal deposit) {
        Customer owner = customerService.findByCitizenId(citizenId)
                .orElseThrow(() -> new BadRequestException("Customer not found."));
        return accountService.createAccount(owner, deposit);
    }

    @PreAuthorize("hasRole('CUSTOMER') and #citizenId == authentication.name")
    @GetMapping("/info")
    public Customer accountInfo(@RequestParam String citizenId) {
        return customerService.findByCitizenId(citizenId).orElseThrow();
    }
}
