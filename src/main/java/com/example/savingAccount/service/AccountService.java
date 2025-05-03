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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public Account createAccount(Customer owner, BigDecimal initialDeposit) {
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
                .orElseThrow(() -> new NotFoundException("Account not found"));
        acc.setBalance(acc.getBalance().add(amount));
        Account updated = accountRepository.save(acc);
        recordTransaction(updated, "DEPOSIT", "DEP", channel, amount, "Deposit");
        return updated;
    }

    @Transactional
    public TransferRequest transferVerify(TransferRequest request) {

        String fromAcc = request.getAccountNumberFrom();
        String toAcc = request.getAccountNumberTo();
        BigDecimal amount = request.getAmount();
        if (fromAcc.equals(toAcc)) {
            throw new BadRequestException("Cannot transfer to the same account");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be greater than 0");
        }
        Account from = accountRepository.findByAccountNumber(fromAcc)
                .orElseThrow(() -> new NotFoundException("Sender account not found"));
        Account to = accountRepository.findByAccountNumber(toAcc)
                .orElseThrow(() -> new NotFoundException("Receiver account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

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
                .orElseThrow(() -> new NotFoundException("Sender account not found"));

        Account to = accountRepository.findByAccountNumber(toAcc)
                .orElseThrow(() -> new NotFoundException("Receiver account not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.save(from);
        accountRepository.save(to);

        Transaction fromTxn = recordTransaction(from, "TRANSFER_OUT", "TFO", channel, amount, "Transfer to " + to.getAccountNumber());
        recordTransaction(to, "TRANSFER_IN", "TFI", channel, amount, "Transfer from " + from.getAccountNumber());
        return fromTxn;
    }

    public List<Transaction> getStatement(String accNum, int year, int month) {
        Account acc = accountRepository.findByAccountNumber(accNum)
                .orElseThrow(() -> new NotFoundException("Account not found"));

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
