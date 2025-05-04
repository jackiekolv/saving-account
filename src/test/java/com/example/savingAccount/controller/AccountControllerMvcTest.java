package com.example.savingAccount.controller;

import com.example.savingAccount.config.AccountPermissionEvaluator;
import com.example.savingAccount.config.SecurityConfig;
import com.example.savingAccount.config.TestConfig;
import com.example.savingAccount.dto.TransferRequest;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.service.AccountService;
import com.example.savingAccount.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest
@Import({TestConfig.class, SecurityConfig.class})
public class AccountControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AccountService accountService;

    @Autowired
    private CustomerService customerService;
    @Autowired
    private AccountPermissionEvaluator authAccount;

    @BeforeEach
    void setup() {

        // 1234567890101 for valid access.
        when(authAccount.isOwner("1234101", "1234567890101")).thenReturn(true);
        // 0000000000000 for invalid access.
        when(authAccount.isOwner("1234101", "0000000000000")).thenReturn(false);

    }
    @Test
    @WithMockUser(roles = "TELLER")
    public void tellerCreateAccount_success() throws Exception {
        Customer mockCustomer = new Customer();
        mockCustomer.setCitizenId("1234567890101");

        Account account = new Account();
        account.setAccountNumber("1234101");

        when(customerService.findByCitizenId(any())).thenReturn(Optional.of(mockCustomer));
        when(accountService.createAccount(any(), any())).thenReturn(account);
        mockMvc.perform(post("/api/account/create")
                        .param("citizenId", "1234567890101")
                        .param("deposit", "1.0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1234101"));
    }

    @Test
    @WithMockUser(roles = "TELLER")
    public void tellerCreateAccount_customer_not_found() throws Exception {
        when(customerService.findByCitizenId(any())).thenReturn(Optional.empty());
        mockMvc.perform(post("/api/account/create")
                        .param("citizenId", "1234567890101")
                        .param("deposit", "1.0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Customer not found for citizenId: 1234567890101"));
    }

    @Test
    public void createAccount_withoutRole_shouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/account/create")
                .param("citizenId", "1234567890101")
                .param("deposit", "1.0"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    public void getAccountInfo_success() throws Exception {
        Customer customer = new Customer();
        customer.setCitizenId("1234567890101");

        when(customerService.findByCitizenId("1234567890101")).thenReturn(Optional.of(customer));

        mockMvc.perform(get("/api/account/info")
                .param("citizenId", "1234567890101"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.citizenId").value("1234567890101"));
    }

    @Test
    @WithMockUser(username = "0000000000000", roles = "CUSTOMER")
    public void getAccountInfo_wrongUser_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/account/info")
                .param("citizenId", "1234567890101"))
            .andExpect(status().isForbidden());
    }
}