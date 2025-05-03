package com.example.savingAccount.repository;

import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndTimestampBetweenOrderByTimestampAsc(Account account, LocalDateTime from, LocalDateTime to);
}
