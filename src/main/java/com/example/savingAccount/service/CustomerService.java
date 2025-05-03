package com.example.savingAccount.service;

import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Customer register(Customer customer) {
        return customerRepository.save(customer);
    }

    public Optional<Customer> findByCitizenId(String citizenId) {
        return customerRepository.findByCitizenId(citizenId);
    }
}
