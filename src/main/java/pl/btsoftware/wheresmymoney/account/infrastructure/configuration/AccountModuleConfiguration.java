package pl.btsoftware.wheresmymoney.account.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.wheresmymoney.account.AccountModuleFacade;
import pl.btsoftware.wheresmymoney.account.application.AccountService;
import pl.btsoftware.wheresmymoney.account.domain.AccountRepository;
import pl.btsoftware.wheresmymoney.account.infrastructure.api.AccountController;
import pl.btsoftware.wheresmymoney.account.infrastructure.persistance.InMemoryAccountRepository;

@Configuration
public class AccountModuleConfiguration {
    @Bean
    public InMemoryAccountRepository inMemoryAccountRepository() {
        return new InMemoryAccountRepository();
    }

    @Bean
    public AccountService accountService(AccountRepository accountRepository) {
        return new AccountService(accountRepository);
    }

    @Bean
    public AccountModuleFacade accountModuleFacade(AccountService accountService) {
        return new AccountModuleFacade(accountService);
    }

    @Bean
    public AccountController accountController(AccountModuleFacade accountModuleFacade) {
        return new AccountController(accountModuleFacade);
    }
}