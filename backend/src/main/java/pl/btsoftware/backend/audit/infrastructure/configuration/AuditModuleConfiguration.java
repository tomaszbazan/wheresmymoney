package pl.btsoftware.backend.audit.infrastructure.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.btsoftware.backend.audit.application.AuditLogService;
import pl.btsoftware.backend.audit.infrastructure.api.AuditLogController;
import pl.btsoftware.backend.shared.pagination.PaginationValidator;
import pl.btsoftware.backend.users.UsersModuleFacade;

@Configuration
public class AuditModuleConfiguration {

    @Bean
    public AuditLogController auditLogController(
            AuditLogService auditLogService,
            UsersModuleFacade usersModuleFacade,
            PaginationValidator paginationValidator) {
        return new AuditLogController(auditLogService, usersModuleFacade, paginationValidator);
    }
}
