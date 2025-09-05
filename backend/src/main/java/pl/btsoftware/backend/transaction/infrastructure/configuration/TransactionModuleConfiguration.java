package pl.btsoftware.backend.transaction.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.transaction.infrastructure.api.TransactionController;
import pl.btsoftware.backend.transaction.infrastructure.persistance.JpaTransactionRepository;
import pl.btsoftware.backend.transaction.infrastructure.persistance.TransactionJpaRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;

@Configuration
public class TransactionModuleConfiguration {

    @Bean
    public TransactionRepository transactionRepository(TransactionJpaRepository transactionJpaRepository) {
        return new JpaTransactionRepository(transactionJpaRepository);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository, AccountModuleFacade accountModuleFacade, UsersModuleFacade usersModuleFacade) {
        return new TransactionService(transactionRepository, accountModuleFacade, usersModuleFacade);
    }

    @Bean
    public TransactionModuleFacade transactionModuleFacade(TransactionService transactionService, UsersModuleFacade usersModuleFacade) {
        return new TransactionModuleFacade(transactionService, usersModuleFacade);
    }

    @Bean
    public TransactionController transactionController(TransactionModuleFacade transactionModuleFacade) {
        return new TransactionController(transactionModuleFacade);
    }

}