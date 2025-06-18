package pl.btsoftware.backend.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.ExpenseRepository;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.backend.account.infrastructure.persistance.InMemoryExpenseRepository;

@Configuration
@Profile("test")
public class TestRepositoryConfiguration {

    @Bean
    @Primary
    public AccountRepository accountRepository() {
        return new InMemoryAccountRepository();
    }

    @Bean
    @Primary
    public ExpenseRepository expenseRepository() {
        return new InMemoryExpenseRepository();
    }
}