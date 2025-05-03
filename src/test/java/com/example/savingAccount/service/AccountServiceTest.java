package com.example.savingAccount.service;

import com.example.savingAccount.dto.TransferRequest;
import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.entity.Transaction;
import com.example.savingAccount.exception.BadRequestException;
import com.example.savingAccount.exception.NotFoundException;
import com.example.savingAccount.repository.AccountRepository;
import com.example.savingAccount.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    void createAccount_shouldSucceed() {
        Customer customer = new Customer();
        customer.setCitizenId("1234567890101");

        BigDecimal balance = BigDecimal.ONE;

        Account account = new Account();
        account.setBalance(BigDecimal.ONE);

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Account result = accountService.createAccount(customer, balance);
        assertTrue(result.getAccountNumber().matches("\\d{7}"), "Account number should be 7 digits");
    }

    @Test
    void deposit_shouldSucceed() {
        String accountNumber = "1234101";
        Customer customer = new Customer();
        customer.setCitizenId("1234567890101");

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomer(customer);
        account.setBalance(BigDecimal.ONE);

        when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Account result = accountService.deposit(accountNumber, BigDecimal.ONE, "BRANCH001");
        assertTrue(result.getAccountNumber().matches("\\d{7}"), "Account number should be 7 digits");
        assertEquals(new BigDecimal("2"), result.getBalance(), "Balance should be increased by deposit amount");
    }

    @Test
    void deposit_shouldThrowIfAccountNotFound() {
        String accountNumber = "1234101";
        Customer customer = new Customer();
        customer.setCitizenId("1234567890101");

        Account account = new Account();
        account.setAccountNumber(accountNumber);
        account.setCustomer(customer);
        account.setBalance(BigDecimal.ONE);

        when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            accountService.deposit(accountNumber, BigDecimal.ONE, "BRANCH001");
        });
        assertEquals("Account not found", exception.getMessage());

    }

    @Test
    void transferVerify_shouldSucceed() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(100));

        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(200));
        Account to = new Account();

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("1234102")).thenReturn(Optional.of(to));

        TransferRequest result = accountService.transferVerify(request);
        assertEquals("Transfer Verify Successful, Please confirm within 60 seconds.", result.getMessage());
    }

    @Test
    void transferVerify_shouldThrowIfSameAccount() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234567");
        request.setAccountNumberTo("1234567");
        request.setAmount(BigDecimal.valueOf(100));
        assertThrows(BadRequestException.class, () -> accountService.transferVerify(request));
    }

    @Test
    void transferVerify_shouldThrowIfInsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(100));

        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(50));
        Account to = new Account();

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("1234102")).thenReturn(Optional.of(to));

        assertThrows(BadRequestException.class, () -> accountService.transferVerify(request));
    }

    @Test
    void transferVerify_shouldThrowIfAmountIsZeroOrNegative() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");

        // Case: amount is zero
        request.setAmount(BigDecimal.ZERO);
        BadRequestException ex1 = assertThrows(BadRequestException.class, () -> accountService.transferVerify(request));
        assertEquals("Transfer amount must be greater than 0", ex1.getMessage());

        // Case: amount is negative
        request.setAmount(BigDecimal.valueOf(-50));
        BadRequestException ex2 = assertThrows(BadRequestException.class, () -> accountService.transferVerify(request));
        assertEquals("Transfer amount must be greater than 0", ex2.getMessage());
    }

    @Test
    void transferConfirm_shouldSucceed() {
        // Arrange
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(100));
        request.setChannel("MOBILE");

        Account from = new Account();
        from.setAccountNumber("1234101");
        from.setBalance(BigDecimal.valueOf(200));

        Account to = new Account();
        to.setAccountNumber("1234102");
        to.setBalance(BigDecimal.valueOf(300));

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("1234102")).thenReturn(Optional.of(to));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(1L); // mock ID
            return txn;
        });

        // Act
        Transaction result = accountService.transferConfirm(request);

        // Assert
        assertEquals("TFO", result.getCode());
        assertEquals(BigDecimal.valueOf(100), result.getAmount());
        assertEquals(BigDecimal.valueOf(100), from.getBalance()); // 200 - 100
        assertEquals(BigDecimal.valueOf(400), to.getBalance());   // 300 + 100
    }

    @Test
    void transferConfirm_shouldThrowIfSenderNotFound() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(100));
        request.setChannel("MOBILE");

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> accountService.transferConfirm(request));
        assertEquals("Sender account not found", ex.getMessage());
    }

    @Test
    void transferConfirm_shouldThrowIfReceiverNotFound() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(100));
        request.setChannel("MOBILE");

        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(200));

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("1234102")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> accountService.transferConfirm(request));
        assertEquals("Receiver account not found", ex.getMessage());
    }

    @Test
    void transferConfirm_shouldThrowIfInsufficientBalance() {
        TransferRequest request = new TransferRequest();
        request.setAccountNumberFrom("1234101");
        request.setAccountNumberTo("1234102");
        request.setAmount(BigDecimal.valueOf(300));
        request.setChannel("MOBILE");

        Account from = new Account();
        from.setBalance(BigDecimal.valueOf(200));

        Account to = new Account();
        to.setBalance(BigDecimal.valueOf(100));

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(from));
        when(accountRepository.findByAccountNumber("1234102")).thenReturn(Optional.of(to));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> accountService.transferConfirm(request));
        assertEquals("Insufficient balance", ex.getMessage());
    }

    @Test
    void getStatement_shouldReturnTransactions() {
        Account acc = new Account();
        acc.setAccountNumber("1234101");

        when(accountRepository.findByAccountNumber("1234101")).thenReturn(Optional.of(acc));
        when(transactionRepository.findByAccountAndTimestampBetweenOrderByTimestampAsc(
                any(), any(), any())).thenReturn(List.of(new Transaction(), new Transaction()));

        List<Transaction> result = accountService.getStatement("1234101", 2024, 7);
        assertEquals(2, result.size());
    }
}