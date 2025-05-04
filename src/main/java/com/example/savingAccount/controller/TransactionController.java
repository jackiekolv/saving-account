package com.example.savingAccount.controller;

import com.example.savingAccount.dto.ConfirmGroup;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.dto.DepositRequest;
import com.example.savingAccount.dto.TransferRequest;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.service.AccountService;
import com.example.savingAccount.service.PinValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/transaction")
@Validated
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final AccountService accountService;
    private final PinValidator pinValidator;
    private final CacheManager cacheManager;

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/deposit")
    public Account deposit(@Valid @RequestBody DepositRequest request) {
        log.info("Deposit request received: account={}, amount={}, channel={}", request.getAccountNumber(), request.getAmount(), request.getChannel());
        return accountService.deposit(request.getAccountNumber(), request.getAmount(), request.getChannel());
    }


    @PostMapping("/transfer/verify")
    @PreAuthorize("hasRole('CUSTOMER') and @authAccount.isOwner(#request.accountNumberFrom, authentication.name)")
    @Cacheable(value = "verifyCache", key = "#request.accountNumberFrom")
    public TransferRequest transferVerify(@Valid @RequestBody TransferRequest request, Authentication authentication) {
        log.info("Transfer verify request by {}: {}", authentication.getName(), request);
        return accountService.transferVerify(request);
    }

    @Validated(ConfirmGroup.class)
    @PostMapping("/transfer/confirm")
    @PreAuthorize("hasRole('CUSTOMER') and @authAccount.isOwner(#request.accountNumberFrom, authentication.name)")
    @CacheEvict(value = "verifyCache", key = "#request.accountNumberFrom")
    public ResponseEntity<?> transferConfirm(@Valid @RequestBody TransferRequest request,
                                      Authentication authentication) {
        log.info("Transfer confirm request by {}: {}", authentication.getName(), request);

        String key = request.getAccountNumberFrom();
        TransferRequest requestVerified = Objects.requireNonNull(cacheManager.getCache("verifyCache")).get(key, TransferRequest.class);

        if (requestVerified == null) {
            log.warn("No verified request found in cache for account: {}", key);
            throw new BadRequestException("Please verify before confirming");
        }else if (!request.equals(requestVerified)) {
            log.warn("Request mismatch: verified={}, confirm={}", requestVerified, request);
            throw new BadRequestException("Transfer data mismatch. Please re-verify before confirming.");
        }

        String citizenId = authentication.getName();
        pinValidator.validate(citizenId, request.getPin());
        log.info("PIN validated and transfer confirmed for account: {}", key);
        Transaction fromTxn = accountService.transferConfirm(requestVerified);

        return ResponseEntity.ok(Map.of(
                "message", "Transfer Confirm Successful.",
                "transaction", fromTxn));
    }
}
