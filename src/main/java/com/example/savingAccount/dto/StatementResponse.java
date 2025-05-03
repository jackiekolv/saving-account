package com.example.savingAccount.dto;

import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class StatementResponse {
    private Account account;
    private List<Transaction> transactions;
}