package com.example.savingAccount.service;

import com.example.savingAccount.dto.TransferRequest;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.repository.AccountRepository;
import com.example.savingAccount.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account createAccount(Customer owner, BigDecimal initialDeposit) {
        log.info("Creating account for customer: {}", owner.getCitizenId());
        String accNum = generateAccountNumber();
        Account account = Account.builder()
                .accountNumber(accNum)
                .customer(owner)
                .balance(initialDeposit != null ? initialDeposit : BigDecimal.ZERO)
                .build();
        Account saved = accountRepository.save(account);
        if (initialDeposit != null && initialDeposit.compareTo(BigDecimal.ZERO) > 0) {
            recordTransaction(saved, "DEPOSIT", "DEP", "BRANCH001", initialDeposit, "Initial deposit");
        }
        log.info("Account created: {}", saved.getAccountNumber());
        return saved;
    }

    private String generateAccountNumber() {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < 7; i++) {
            sb.append(rand.nextInt(10));
        }
        return sb.toString();
    }

    @Transactional
    public Account deposit(String accNum, BigDecimal amount, String channel) {
        Account acc = accountRepository.findByAccountNumber(accNum)
                .orElseThrow(() -> {
                    log.warn("Account not found for deposit: {}", accNum);
                    return new NotFoundException("Account not found");
                });
        log.info("Depositing {} to account {}", amount, accNum);
        acc.setBalance(acc.getBalance().add(amount));
        Account updated = accountRepository.save(acc);
        recordTransaction(updated, "DEPOSIT", "DEP", channel, amount, "Deposit");
        log.info("New balance for account {}: {}", acc.getAccountNumber(), acc.getBalance());
        return updated;
    }

    @Transactional
    public TransferRequest transferVerify(TransferRequest request) {

        String fromAcc = request.getAccountNumberFrom();
        String toAcc = request.getAccountNumberTo();
        BigDecimal amount = request.getAmount();
        if (fromAcc.equals(toAcc)) {
            log.warn("Cannot transfer to the same account: {}", fromAcc);
            throw new BadRequestException("Cannot transfer to the same account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Transfer amount must be greater than 0: {}", amount);
            throw new BadRequestException("Transfer amount must be greater than 0");
        }
        Account from = accountRepository.findByAccountNumber(fromAcc)
                .orElseThrow(() -> {
                    log.warn("Sender account not found: {}", fromAcc);
                    return new NotFoundException("Sender account not found");
                });
        Account to = accountRepository.findByAccountNumber(toAcc)
                .orElseThrow(() -> {
                    log.warn("Receiver account not found: {}", toAcc);
                    return new NotFoundException("Receiver account not found");
                });

        if (from.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance in account {} for amount {}", fromAcc, amount);
            throw new BadRequestException("Insufficient balance");
        }
        log.info("Verifying transfer from {} to {} amount {}", fromAcc, toAcc, amount);
        log.debug("Transfer verification passed. Awaiting confirmation.");

        request.setMessage("Transfer Verify Successful, Please confirm within 60 seconds.");
        return request;
    }

    @Transactional
    public Transaction transferConfirm(TransferRequest request) {

        String fromAcc = request.getAccountNumberFrom();
        String toAcc = request.getAccountNumberTo();
        String channel = request.getChannel();
        BigDecimal amount = request.getAmount();

        Account from = accountRepository.findByAccountNumber(fromAcc)
                .orElseThrow(() -> {
                    log.warn("Sender account not found: {}", fromAcc);
                    return new NotFoundException("Sender account not found");
                });

        Account to = accountRepository.findByAccountNumber(toAcc)
                .orElseThrow(() -> {
                    log.warn("Receiver account not found: {}", toAcc);
                    return new NotFoundException("Receiver account not found");
                });

        if (from.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient balance in account {} for amount {}", fromAcc, amount);
            throw new BadRequestException("Insufficient balance");
        }
        log.info("Confirming transfer from {} to {} amount {}", fromAcc, toAcc, amount);
        log.debug("From account balance before: {}", from.getBalance().add(amount));
        log.debug("To account balance before: {}", to.getBalance().subtract(amount));

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction fromTxn = recordTransaction(from, "TRANSFER_OUT", "TFO", channel, amount, "Transfer to " + to.getAccountNumber());
        recordTransaction(to, "TRANSFER_IN", "TFI", channel, amount, "Transfer from " + from.getAccountNumber());
        log.info("Transfer confirmed. From: {}, To: {}", from.getAccountNumber(), to.getAccountNumber());
        return fromTxn;
    }

    public List<Transaction> getStatement(String accNum, int year, int month) {
        Account acc = accountRepository.findByAccountNumber(accNum)
                .orElseThrow(() -> {
                    log.warn("Account not found for statement: {}", accNum);
                    return new NotFoundException("Account not found");
                });
        log.info("Getting statement for account {} for {}/{}", accNum, month, year);

        LocalDateTime start = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime end = start.plusMonths(1).minusNanos(1);

        return transactionRepository.findByAccountAndTimestampBetweenOrderByTimestampAsc(acc, start, end);
    }

    private Transaction recordTransaction(Account account, String type, String code, String channel, BigDecimal amount, String remark) {
        Transaction txn = Transaction.builder()
                .timestamp(LocalDateTime.now())
                .account(account)
                .type(type)
                .code(code)
                .channel(channel)
                .amount(amount)
                .balanceAfter(account.getBalance())
                .remark(remark)
                .build();
        return transactionRepository.save(txn);
    }
}
