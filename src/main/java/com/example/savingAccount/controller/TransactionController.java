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

    private final AccountService accountService;
    private final PinValidator pinValidator;
    private final CacheManager cacheManager;

    @PreAuthorize("hasRole('TELLER')")
    @PostMapping("/deposit")
    public Account deposit(@Valid @RequestBody DepositRequest request) {
        return accountService.deposit(request.getAccountNumber(), request.getAmount(), request.getChannel());
    }


    @PostMapping("/transfer/verify")
    @PreAuthorize("hasRole('CUSTOMER') and @authAccount.isOwner(#request.accountNumberFrom, authentication.name)")
    @Cacheable(value = "verifyCache", key = "#request.accountNumberFrom")
    public TransferRequest transferVerify(@Valid @RequestBody TransferRequest request, Authentication authentication) {

        return accountService.transferVerify(request);
    }

    @Validated(ConfirmGroup.class)
    @PostMapping("/transfer/confirm")
    @PreAuthorize("hasRole('CUSTOMER') and @authAccount.isOwner(#request.accountNumberFrom, authentication.name)")
    @CacheEvict(value = "verifyCache", key = "#request.accountNumberFrom")
    public ResponseEntity<?> transferConfirm(@Valid @RequestBody TransferRequest request,
                                      Authentication authentication) {

        String key = request.getAccountNumberFrom();
        TransferRequest requestVerified = Objects.requireNonNull(cacheManager.getCache("verifyCache")).get(key, TransferRequest.class);

        if (requestVerified == null) {
            throw new BadRequestException("Please verify before confirming");
        }else if (!request.equals(requestVerified)) {
            throw new BadRequestException("Verify and Confirm does not matched, Please perform new verification.");
        }

        String citizenId = authentication.getName();
        pinValidator.validate(citizenId, request.getPin());
        Transaction fromTxn = accountService.transferConfirm(requestVerified);

        return ResponseEntity.ok(Map.of(
                "message", "Transfer Confirm Successful.",
                "transaction", fromTxn));
    }
}
