package pl.btsoftware.backend.audit.infrastructure.api;

import org.springframework.data.domain.Page;
import pl.btsoftware.backend.audit.domain.AuditLog;

import java.util.List;

public record AuditLogsView(
        List<AuditLogView> auditLogs,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static AuditLogsView from(Page<AuditLog> auditLogPage) {
        var auditLogs = auditLogPage.getContent().stream()
                .map(AuditLogView::from)
                .toList();

        return new AuditLogsView(
                List.copyOf(auditLogs),
                auditLogPage.getNumber(),
                auditLogPage.getSize(),
                auditLogPage.getTotalElements(),
                auditLogPage.getTotalPages()
        );
    }
}
