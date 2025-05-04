package com.example.savingAccount.controller;

import com.example.savingAccount.dto.StatementResponse;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statement")
@RequiredArgsConstructor
public class StatementController {

    private static final Logger log = LoggerFactory.getLogger(StatementController.class);

    private final AccountService accountService;

    @PreAuthorize("hasRole('CUSTOMER') and @authAccount.isOwner(#accountNumber, authentication.name)")
    @GetMapping
    public ResponseEntity<?> getMonthlyStatement(@RequestParam String accountNumber, @RequestParam int year, @RequestParam int month) {
        log.info("Fetching statement for account {}, year {}, month {}", accountNumber, year, month);
        List<Transaction> transactions = accountService.getStatement(accountNumber, year, month);
        if (transactions.isEmpty()) {
            log.warn("No transactions found for account {}, year {}, month {}", accountNumber, year, month);
            throw new NotFoundException("No transactions found for the specified month.");
        }
        log.info("Returning {} transactions for account {}", transactions.size(), accountNumber);
        Account account = transactions.get(0).getAccount();
        return ResponseEntity.ok(new StatementResponse(account, transactions));
    }
}
