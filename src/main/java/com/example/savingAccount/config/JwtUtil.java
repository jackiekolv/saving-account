package com.example.savingAccount.config;


import com.example.savingAccount.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    private final String SECRET = "my-super-secret-key-for-jackie-must-be-very-long-256bit";
    private final long EXPIRATION_MS = 86400000; // 1 วัน

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(String citizenId, Role role) {
        String token = Jwts.builder()
                .setSubject(citizenId)
                .claim("role", role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
        log.info("Generated token for citizenId: {}, role: {}", citizenId, role);
        return token;
    }

    public Claims validateAndExtract(String token) {
        try {
            Claims claims = Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token)
                    .getBody();
            log.debug("Token validated successfully for citizenId: {}", claims.getSubject());
            return claims;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }
}