package com.example.savingAccount.controller;

import com.example.savingAccount.config.JwtUtil;
import com.example.savingAccount.dto.LoginRequest;
import com.example.savingAccount.entity.Role;
import com.example.savingAccount.entity.Teller;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.exception.UnauthorizedException;
import com.example.savingAccount.repository.TellerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/auth/teller")
@RequiredArgsConstructor
@Slf4j
public class AuthTellerController {

    private final TellerRepository tellerRepository;
    private final JwtUtil jwtUtil;

    // -------------------- Login --------------------
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempting login for teller with citizenId: {}", request.getCitizenId());
        Teller teller = tellerRepository.findByCitizenId(request.getCitizenId())
                .orElseThrow(() -> {
                    log.warn("Teller not found for citizenId: {}", request.getCitizenId());
                    return new UnauthorizedException("Teller not found for citizenId: " + request.getCitizenId());
                });

        if (!BCrypt.checkpw(request.getPassword(), teller.getPassword())) {
            log.warn("Invalid password for teller with citizenId: {}", request.getCitizenId());
            throw new UnauthorizedException("Invalid password for citizenId: " + request.getCitizenId());
        }

        String jwt = jwtUtil.generateToken(teller.getCitizenId(), Role.TELLER);
        log.info("Login successful for teller with citizenId: {}", request.getCitizenId());
        return Map.of("token", jwt);
    }



}
