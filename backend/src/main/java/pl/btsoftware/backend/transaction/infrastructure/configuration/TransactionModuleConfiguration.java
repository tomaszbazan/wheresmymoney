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

@Configuration
public class TransactionModuleConfiguration {

    @Bean
    public TransactionRepository transactionRepository(TransactionJpaRepository transactionJpaRepository) {
        return new JpaTransactionRepository(transactionJpaRepository);
    }

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository, AccountModuleFacade accountModuleFacade) {
        return new TransactionService(transactionRepository, accountModuleFacade);
    }

    @Bean
    public TransactionModuleFacade transactionModuleFacade(TransactionService transactionService) {
        return new TransactionModuleFacade(transactionService);
    }

    @Bean
    public TransactionController transactionController(TransactionModuleFacade transactionModuleFacade) {
        return new TransactionController(transactionModuleFacade);
    }

}