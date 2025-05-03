package com.example.savingAccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

    private String type; // DEPOSIT, TRANSFER_IN, TRANSFER_OUT

    private String code; // DEP, TFI, TFO

    private String channel;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String remark;
}
