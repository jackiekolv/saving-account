package com.example.savingAccount.service;

import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void testFindCustomerById_shouldReturnCustomer() {
        Customer customer = new Customer();
        customer.setCitizenId("1234567890123");

        when(customerRepository.findByCitizenId("1234567890123"))
            .thenReturn(Optional.of(customer));

        Optional<Customer> result = customerService.findByCitizenId("1234567890123");

        assertTrue(result.isPresent());
        assertEquals("1234567890123", result.get().getCitizenId());
    }

    @Test
    void testFindCustomerById_shouldReturnEmptyIfNotFound() {
        when(customerRepository.findByCitizenId("notfound"))
            .thenReturn(Optional.empty());

        Optional<Customer> result = customerService.findByCitizenId("notfound");

        assertTrue(result.isEmpty());
    }
}