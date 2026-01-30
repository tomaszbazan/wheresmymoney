package pl.btsoftware.backend.transaction.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.shared.pagination.PaginationValidator;
import pl.btsoftware.backend.transaction.TransactionModuleFacade;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
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
    public TransactionQueryFacade transactionQueryFacade(TransactionRepository transactionRepository) {
        return new TransactionQueryFacade(transactionRepository);
    }

    @Bean
    public TransactionService transactionService(
            TransactionRepository transactionRepository,
            AccountModuleFacade accountModuleFacade,
            CategoryQueryFacade categoryQueryFacade,
            UsersModuleFacade usersModuleFacade,
            AuditModuleFacade auditModuleFacade) {
        return new TransactionService(
                transactionRepository, accountModuleFacade, categoryQueryFacade, usersModuleFacade, auditModuleFacade);
    }

    @Bean
    public TransactionModuleFacade transactionModuleFacade(
            TransactionService transactionService, UsersModuleFacade usersModuleFacade) {
        return new TransactionModuleFacade(transactionService, usersModuleFacade);
    }

    @Bean
    public TransactionController transactionController(
            TransactionModuleFacade transactionModuleFacade,
            CategoryModuleFacade categoryModuleFacade,
            PaginationValidator paginationValidator) {
        return new TransactionController(transactionModuleFacade, categoryModuleFacade, paginationValidator);
    }
}
