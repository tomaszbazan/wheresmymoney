package pl.btsoftware.backend.audit.infrastructure.api;

import java.util.List;
import org.springframework.data.domain.Page;
import pl.btsoftware.backend.audit.domain.AuditLog;

public record AuditLogsView(List<AuditLogView> auditLogs, int page, int size, long totalElements, int totalPages) {
    public AuditLogsView {
        auditLogs = List.copyOf(auditLogs);
    }

    public static AuditLogsView from(Page<AuditLog> auditLogPage) {
        var auditLogs =
                auditLogPage.getContent().stream().map(AuditLogView::from).toList();

        return new AuditLogsView(
                List.copyOf(auditLogs),
                auditLogPage.getNumber(),
                auditLogPage.getSize(),
                auditLogPage.getTotalElements(),
                auditLogPage.getTotalPages());
    }
}
