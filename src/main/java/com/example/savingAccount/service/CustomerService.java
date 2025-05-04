package com.example.savingAccount.service;

import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    public Customer register(Customer customer) {
        log.info("Registering new customer with citizenId: {}", customer.getCitizenId());
        return customerRepository.save(customer);
    }

    public Optional<Customer> findByCitizenId(String citizenId) {
        log.info("Searching for customer with citizenId: {}", citizenId);
        return customerRepository.findByCitizenId(citizenId);
    }
}
