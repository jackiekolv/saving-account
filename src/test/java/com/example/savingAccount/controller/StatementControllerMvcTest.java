package com.example.savingAccount.controller;

import com.example.savingAccount.config.AccountPermissionEvaluator;
import com.example.savingAccount.config.SecurityConfig;
import com.example.savingAccount.config.TestConfig;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebMvcTest(StatementController.class)
@Import({TestConfig.class, SecurityConfig.class})
class StatementControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountPermissionEvaluator authAccount;

    private Account account;
    private Customer customer;
    private Transaction txn;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setCitizenId("1234567890101");

        account = new Account();
        account.setAccountNumber("1234567");
        account.setCustomer(customer);
        account.setBalance(BigDecimal.TEN);

        txn = new Transaction();
        txn.setAccount(account);
        txn.setAmount(BigDecimal.ONE);
        txn.setType("DEPOSIT");

        // 1234567890101 for valid access.
        when(authAccount.isOwner("1234567", "1234567890101")).thenReturn(true);

        // 0000000000000 for invalid access.
        when(authAccount.isOwner("1234567", "0000000000000")).thenReturn(false);

    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testGetMonthlyStatement_shouldReturnOk() throws Exception {
        when(accountService.getStatement("1234567", 2025, 5))
                .thenReturn(List.of(txn));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/statement")
                        .param("accountNumber", "1234567")
                        .param("year", "2025")
                        .param("month", "5"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @WithMockUser(username = "0000000000000", roles = "CUSTOMER")
    void testGetMonthlyStatement_shouldReturnForbidden_whenCitizenIdDoesNotMatch() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/api/statement")
                        .param("accountNumber", "1234567")
                        .param("year", "2025")
                        .param("month", "5"))
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testGetMonthlyStatement_shouldReturnBadRequest_whenMissingParams() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/statement")
                        .param("accountNumber", "1234567")
                        .param("year", "2025"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testGetMonthlyStatement_shouldReturnInternalServerError_whenServiceFails() throws Exception {
        when(accountService.getStatement("1234567", 2025, 6))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/statement")
                        .param("accountNumber", "1234567")
                        .param("year", "2025")
                        .param("month", "6"))
                .andExpect(MockMvcResultMatchers.status().isInternalServerError());
    }
}