package com.example.savingAccount.controller;

import com.example.savingAccount.config.JwtUtil;
import com.example.savingAccount.config.TestConfig;
import com.example.savingAccount.config.SecurityConfig;
import com.example.savingAccount.entity.Teller;
import com.example.savingAccount.repository.TellerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthTellerController.class)
@Import({TestConfig.class, SecurityConfig.class})
public class AuthTellerControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TellerRepository tellerRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void tellerLogin_success() throws Exception {
        Teller mockTeller = new Teller();
        mockTeller.setCitizenId("1234567890501");
        mockTeller.setPassword("123456");
        mockTeller.setPassword(BCrypt.hashpw(mockTeller.getPassword(), BCrypt.gensalt()));

        when(tellerRepository.findByCitizenId(any())).thenReturn(Optional.of(mockTeller));

        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890501",
                                      "password": "123456"
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void tellerLogin_invalid_password() throws Exception {
        Teller mockTeller = new Teller();
        mockTeller.setCitizenId("1234567890501");
        mockTeller.setPassword("123456");
        mockTeller.setPassword(BCrypt.hashpw(mockTeller.getPassword(), BCrypt.gensalt()));

        when(tellerRepository.findByCitizenId(any())).thenReturn(Optional.of(mockTeller));

        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890501",
                                      "password": "1111111"
                                    }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid password for citizenId: 1234567890501"));
    }

    @Test
    void tellerLogin_customer_not_found() throws Exception {

        when(tellerRepository.findByCitizenId(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890109",
                                      "password": "1111111"
                                    }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(startsWith("Teller not found")));
    }

    @Test
    void tellerLogin_blank_citizenId_should_return_bad_request() throws Exception {
        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citizenId": "",
                                  "password": "123456"
                                }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tellerLogin_blank_password_should_return_bad_request() throws Exception {
        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citizenId": "1234567890501",
                                  "password": ""
                                }
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tellerLogin_missing_fields_should_return_bad_request() throws Exception {
        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "123456"
                                }
                            """))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/auth/teller/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "citizenId": "1234567890501"
                                }
                            """))
                .andExpect(status().isBadRequest());
    }
}