package com.example.savingAccount.controller;

import com.example.savingAccount.config.JwtUtil;
import com.example.savingAccount.config.SecurityConfig;
import com.example.savingAccount.config.TestConfig;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.matchesPattern;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import org.assertj.core.api.Assertions;

@WebMvcTest(AuthCustomerController.class)
@Import({TestConfig.class, SecurityConfig.class})
public class AuthCustomerControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void customerRegister_success() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890123");

        when(customerService.findByCitizenId(any())).thenReturn(Optional.empty());
        when(customerService.register(any())).thenReturn(mockCustomer);

        mockMvc.perform(post("/api/auth/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "citizenId": "1234567890123",
                                        "password": "123456",
                                        "pin": "112233",
                                        "email": "test@example.com",
                                        "thaiName": "ชื่อไทย",
                                        "engName": "English Name"
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.citizenId").value("1234567890123"));
    }

    @Test
    void customerRegister_duplicated_citizenId() throws Exception {

        when(customerService.findByCitizenId(any())).thenReturn(Optional.of(new Customer()));

        mockMvc.perform(post("/api/auth/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "citizenId": "1234567890123",
                                        "password": "password123",
                                        "pin": "112233",
                                        "email": "test@example.com",
                                        "thaiName": "ชื่อไทย",
                                        "engName": "English Name"
                                    }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("This Citizen ID is already exist!"));
    }

    @Test
    void customerLogin_success() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890101");
        mockCustomer.setPassword("123456");
        mockCustomer.setPassword(BCrypt.hashpw(mockCustomer.getPassword(), BCrypt.gensalt()));

        when(customerService.findByCitizenId(any())).thenReturn(Optional.of(mockCustomer));

        mockMvc.perform(post("/api/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890101",
                                      "password": "123456"
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void customerLogin_invalid_password() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890101");
        mockCustomer.setPassword("123456");
        mockCustomer.setPassword(BCrypt.hashpw(mockCustomer.getPassword(), BCrypt.gensalt()));

        when(customerService.findByCitizenId(any())).thenReturn(Optional.of(mockCustomer));

        mockMvc.perform(post("/api/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890101",
                                      "password": "1111111"
                                    }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid password"));
    }

    @Test
    void customerLogin_customer_not_found() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890101");
        mockCustomer.setPassword("123456");
        mockCustomer.setPassword(BCrypt.hashpw(mockCustomer.getPassword(), BCrypt.gensalt()));

        when(customerService.findByCitizenId(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890109",
                                      "password": "1111111"
                                    }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void customerLogin_shouldReturnValidJwtTokenFormat() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890101");
        mockCustomer.setPassword("123456");
        mockCustomer.setPassword(BCrypt.hashpw(mockCustomer.getPassword(), BCrypt.gensalt()));

        when(customerService.findByCitizenId(any())).thenReturn(Optional.of(mockCustomer));

        String response = mockMvc.perform(post("/api/auth/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                      "citizenId": "1234567890101",
                                      "password": "123456"
                                    }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String token = JsonPath.read(response, "$.token");

        SecretKey key = Keys.hmacShaKeyFor("my-super-secret-key-for-jackie-must-be-very-long-256bit".getBytes(StandardCharsets.UTF_8));

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Assertions.assertThat(claims.getSubject()).isEqualTo("1234567890101");
        Assertions.assertThat(claims.get("role")).isEqualTo("CUSTOMER");
    }
}