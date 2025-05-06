package pl.btsoftware.wheresmymoney.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryAccountRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryExpenseRepository;

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