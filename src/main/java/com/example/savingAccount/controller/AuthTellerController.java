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
public class AuthTellerController {

    private final TellerRepository tellerRepository;
    private final JwtUtil jwtUtil;

    // -------------------- Login --------------------
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody LoginRequest request) {
        Teller teller = tellerRepository.findByCitizenId(request.getCitizenId())
                .orElseThrow(() -> new UnauthorizedException("Teller not found"));

        if (!BCrypt.checkpw(request.getPassword(), teller.getPassword())) {
            throw new UnauthorizedException("Invalid password");
        }

        String jwt = jwtUtil.generateToken(teller.getCitizenId(), Role.TELLER);
        return Map.of("token", jwt);
    }



}
