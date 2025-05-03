package com.example.savingAccount.repository;

import com.example.savingAccount.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByCitizenId(String citizenId);
}
