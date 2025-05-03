package com.example.savingAccount.config;

import com.example.savingAccount.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("authAccount")
@RequiredArgsConstructor
public class AccountPermissionEvaluator {

    private final AccountRepository accountRepository;

    public boolean isOwner(String accountNo, String citizenId) {
        return accountRepository.findByAccountNumber(accountNo)
                .map(acc -> acc.getCustomer().getCitizenId().equals(citizenId))
                .orElse(false);
    }
}