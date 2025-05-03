package com.example.savingAccount.repository;

import com.example.savingAccount.entity.Teller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TellerRepository extends JpaRepository<Teller, Long> {
    Optional<Teller> findByCitizenId(String citizenId);
}
