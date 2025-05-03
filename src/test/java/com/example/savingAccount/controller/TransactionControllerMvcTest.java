package com.example.savingAccount.controller;

import com.example.savingAccount.config.AccountPermissionEvaluator;
import com.example.savingAccount.config.SecurityConfig;
import com.example.savingAccount.config.TestConfig;
import com.example.savingAccount.dto.TransferRequest;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.repository.CustomerRepository;
import com.example.savingAccount.repository.TellerRepository;
import com.example.savingAccount.service.AccountService;
import com.example.savingAccount.service.PinValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Objects;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
@Import({TestConfig.class, SecurityConfig.class})
class TransactionControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private PinValidator pinValidator;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private TellerRepository tellerRepository;

    @MockBean
    private CacheManager cacheManager;

    @Autowired
    private AccountPermissionEvaluator authAccount;

    @Autowired
    private ObjectMapper objectMapper;

    private TransferRequest request;

    @BeforeEach
    void setup() {
        request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.TEN);
        request.setChannel("MOBILEAPP");
        request.setPin("112233");

        // 1234567890101 for valid access.
        when(authAccount.isOwner("1234101", "1234567890101")).thenReturn(true);
        when(authAccount.isOwner("1234103", "1234567890101")).thenReturn(true);

        // 0000000000000 for invalid access.
        when(authAccount.isOwner("1234101", "0000000000000")).thenReturn(false);

        Cache cache = Mockito.mock(Cache.class);
        when(cache.get("1234101", TransferRequest.class)).thenReturn(request);
        when(cacheManager.getCache("verifyCache")).thenReturn(cache);
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testTransferVerify_shouldReturnOk() throws Exception {
        when(accountService.transferVerify(request)).thenReturn(request);

        mockMvc.perform(post("/api/transaction/transfer/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testTransferConfirm_shouldReturnOk() throws Exception {
        // Mock verify cache
        when(accountService.transferVerify(request)).thenReturn(request);
        when(accountService.transferConfirm(request)).thenReturn(new Transaction());
        when(pinValidator.validate("1234567890101", "112233")).thenReturn(true);

        mockMvc.perform(post("/api/transaction/transfer/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "accountNumberFrom": "1234101",
                                        "accountNumberTo": "1234102",
                                        "amount": 10,
                                        "channel": "MOBILEAPP",
                                        "pin": "112233"
                                    }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testTransferConfirm_shouldReturnBadRequest_whenVerifyMissing() throws Exception {

        when(accountService.transferConfirm(request)).thenReturn(new Transaction());

        mockMvc.perform(post("/api/transaction/transfer/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "accountNumberFrom": "1234103",
                                        "accountNumberTo": "1234102",
                                        "amount": 10,
                                        "channel": "MOBILEAPP",
                                        "pin": "112233"
                                    }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testTransferConfirm_shouldReturnBadRequest_whenVerifyMismatch() throws Exception {

        mockMvc.perform(post("/api/transaction/transfer/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "accountNumberFrom": "1234101",
                                        "accountNumberTo": "1234102",
                                        "amount": 9999999999999999999999999999999999,
                                        "channel": "MOBILEAPP",
                                        "pin": "112233"
                                    }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "1234567890101", roles = "CUSTOMER")
    void testTransferConfirm_shouldReturnBadRequest_whenPinInvalid() throws Exception {
        when(pinValidator.validate("1234567890101", "000000")).thenThrow(new BadRequestException("Invalid PIN"));

        mockMvc.perform(post("/api/transaction/transfer/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "accountNumberFrom": "1234101",
                                        "accountNumberTo": "1234102",
                                        "amount": 10,
                                        "channel": "MOBILEAPP",
                                        "pin": "000000"
                                    }
                                """))
                .andExpect(status().isBadRequest());
    }
}