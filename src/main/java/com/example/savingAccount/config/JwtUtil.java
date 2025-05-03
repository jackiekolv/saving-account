package com.example.savingAccount.config;


import com.example.savingAccount.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private final String SECRET = "my-super-secret-key-for-jackie-must-be-very-long-256bit";
    private final long EXPIRATION_MS = 86400000; // 1 วัน

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String citizenId, Role role) {
        return Jwts.builder()
                .setSubject(citizenId)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateAndExtract(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }
}