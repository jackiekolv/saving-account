package com.example.savingAccount.config;

import com.example.savingAccount.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("authAccount")
@RequiredArgsConstructor
public class AccountPermissionEvaluator {

    private static final Logger log = LoggerFactory.getLogger(AccountPermissionEvaluator.class);

    private final AccountRepository accountRepository;

    public boolean isOwner(String accountNo, String citizenId) {
        log.info("Checking ownership for accountNo={} and citizenId={}", accountNo, citizenId);
        boolean isOwner = accountRepository.findByAccountNumber(accountNo)
                .map(acc -> acc.getCustomer().getCitizenId().equals(citizenId))
                .orElse(false);
        log.debug("Ownership check result: {}", isOwner);
        return isOwner;
    }
}