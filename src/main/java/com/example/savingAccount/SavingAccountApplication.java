package com.example.savingAccount;

import com.example.savingAccount.entity.Account;
import com.example.savingAccount.entity.Customer;
import com.example.savingAccount.entity.Teller;
import com.example.savingAccount.repository.CustomerRepository;
import com.example.savingAccount.repository.TellerRepository;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCrypt;

import java.math.BigDecimal;
import java.util.List;

@OpenAPIDefinition(
		info = @Info(title = "Saving Account API", version = "1.0", description = "PoC for Core Banking")
)
@SpringBootApplication
@EnableCaching
public class SavingAccountApplication {

	public static void main(String[] args) {
		SpringApplication.run(SavingAccountApplication.class, args);
	}

	@Bean
	CommandLineRunner initData(CustomerRepository customerRepo, TellerRepository tellerRepo) {
		return args -> {
			if (customerRepo.findByCitizenId("1234567890101").isEmpty()) {
				Account account = Account.builder()
						.accountNumber("1234101")
						.balance(BigDecimal.valueOf(10_000))
						.build();
				Customer user = Customer.builder()
						.email("jackie101@example.com")
						.password(BCrypt.hashpw("123456", BCrypt.gensalt()))
						.pin(BCrypt.hashpw("112233", BCrypt.gensalt()))
						.citizenId("1234567890101")
						.thaiName("ธัชพงศ์ จิรรัตนวงศ์")
						.engName("Thachapong Chirarattanawong")
						.accounts(List.of(account))
						.build();
				account.setCustomer(user);
				customerRepo.save(user);
			}
			if (customerRepo.findByCitizenId("1234567890102").isEmpty()) {
				Account account = Account.builder()
						.accountNumber("1234102")
						.balance(BigDecimal.valueOf(10_000))
						.build();
				Customer user = Customer.builder()
						.email("john102@example.com")
						.password(BCrypt.hashpw("123456", BCrypt.gensalt()))
						.pin(BCrypt.hashpw("112233", BCrypt.gensalt()))
						.citizenId("1234567890102")
						.thaiName("จอห์น วิค")
						.engName("John Wick")
						.accounts(List.of(account))
						.build();
				account.setCustomer(user);
				customerRepo.save(user);
			}

			if (tellerRepo.findByCitizenId("1234567890501").isEmpty()) {
				Teller teller = Teller.builder()
						.email("teller501@bank.com")
						.password(BCrypt.hashpw("123456", BCrypt.gensalt()))
						.citizenId("1234567890501")
						.employeeId("EMP501")
						.thaiName("เทลเลอร์ห้าศูนย์หนึ่ง ทดสอบ")
						.engName("Teller501 Test")
						.build();
				tellerRepo.save(teller);
			}

			System.out.println("✅ Sample users initialized");
		};
	}
}