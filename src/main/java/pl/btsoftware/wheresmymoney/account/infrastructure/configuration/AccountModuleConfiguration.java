package pl.btsoftware.wheresmymoney.account.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.application.AccountService;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;
import pl.btsoftware.wheresmymoney.account.domain.ExpenseRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.api.AccountController;
import pl.btsoftware.wheresmymoney.account.infrastructure.api.ExpenseController;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.AccountJpaRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.ExpenseJpaRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.JpaAccountRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.JpaExpenseRepository;

@Configuration
public class AccountModuleConfiguration {

    @Bean
    public AccountRepository accountRepository(AccountJpaRepository accountJpaRepository) {
        return new JpaAccountRepository(accountJpaRepository);
    }

    @Bean
    public ExpenseRepository expenseRepository(ExpenseJpaRepository expenseJpaRepository) {
        return new JpaExpenseRepository(expenseJpaRepository);
    }

    @Bean
    public AccountService accountService(AccountRepository accountRepository, ExpenseRepository expenseRepository) {
        return new AccountService(accountRepository, expenseRepository);
    }

    @Bean
    public AccountModuleFacade accountModuleFacade(AccountService accountService) {
        return new AccountModuleFacade(accountService);
    }

    @Bean
    public AccountController accountController(AccountModuleFacade accountModuleFacade) {
        return new AccountController(accountModuleFacade);
    }

    @Bean
    public ExpenseController expenseController(AccountModuleFacade accountModuleFacade) {
        return new ExpenseController(accountModuleFacade);
    }
}
