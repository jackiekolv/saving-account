package com.example.savingAccount.config;

import com.example.savingAccount.repository.AccountRepository;
import com.example.savingAccount.service.PinValidator;

import com.example.savingAccount.config.JwtUtil;
import com.example.savingAccount.repository.TellerRepository;
import com.example.savingAccount.repository.CustomerRepository;
import com.example.savingAccount.service.AccountService;
import com.example.savingAccount.service.CustomerService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Bean
    public TellerRepository tellerRepository() {
        return Mockito.mock(TellerRepository.class);
    }

    @Bean
    public CustomerService customerService() {
        return Mockito.mock(CustomerService.class);
    }

    @Bean
    public AccountService accountService() {
        return Mockito.mock(AccountService.class);
    }

    @Bean
    public PinValidator pinValidator() {
        return Mockito.mock(PinValidator.class);
    }

    @Bean
    public CustomerRepository customerRepository() {
        return Mockito.mock(CustomerRepository.class);
    }

    @Bean
    public AccountRepository accountRepository() {
        return Mockito.mock(AccountRepository.class);
    }

    @Bean
    public AccountPermissionEvaluator authAccount() {
        return Mockito.mock(AccountPermissionEvaluator.class);
    }

}