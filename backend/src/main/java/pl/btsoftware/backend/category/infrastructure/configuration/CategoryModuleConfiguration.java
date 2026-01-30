package pl.btsoftware.backend.category.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.category.application.CategoryService;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.category.infrastructure.api.CategoryController;
import pl.btsoftware.backend.category.infrastructure.persistance.CategoryJpaRepository;
import pl.btsoftware.backend.category.infrastructure.persistance.JpaCategoryRepository;
import pl.btsoftware.backend.transaction.TransactionQueryFacade;
import pl.btsoftware.backend.users.UsersModuleFacade;

@Configuration
public class CategoryModuleConfiguration {

    @Bean
    @Profile("test")
    public CategoryRepository categoryRepository(CategoryJpaRepository categoryJpaRepository) {
        return new JpaCategoryRepository(categoryJpaRepository);
    }

    @Bean
    public CategoryQueryFacade categoryQueryFacade(CategoryRepository categoryRepository) {
        return new CategoryQueryFacade(categoryRepository);
    }

    @Bean
    public CategoryService categoryService(
            CategoryRepository categoryRepository,
            UsersModuleFacade usersModuleFacade,
            TransactionQueryFacade transactionQueryFacade,
            AuditModuleFacade auditModuleFacade) {
        return new CategoryService(categoryRepository, usersModuleFacade, transactionQueryFacade, auditModuleFacade);
    }

    @Bean
    public CategoryModuleFacade categoryModuleFacade(
            CategoryService categoryService, UsersModuleFacade usersModuleFacade) {
        return new CategoryModuleFacade(categoryService, usersModuleFacade);
    }

    @Bean
    public CategoryController categoryController(CategoryModuleFacade categoryModuleFacade) {
        return new CategoryController(categoryModuleFacade);
    }
}
