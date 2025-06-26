package pl.btsoftware.backend.account.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.application.AccountService;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.infrastructure.api.AccountController;
import pl.btsoftware.backend.account.infrastructure.persistance.AccountJpaRepository;
import pl.btsoftware.backend.account.infrastructure.persistance.JpaAccountRepository;

@Configuration
public class AccountModuleConfiguration {

    @Bean
    public AccountRepository accountRepository(AccountJpaRepository accountJpaRepository) {
        return new JpaAccountRepository(accountJpaRepository);
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
