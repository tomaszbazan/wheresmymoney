package pl.btsoftware.backend.audit.infrastructure.api;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.audit.application.AuditLogService;
import pl.btsoftware.backend.audit.domain.AuditLogId;
import pl.btsoftware.backend.shared.pagination.PaginationValidator;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {
    private final AuditLogService auditLogService;
    private final UsersModuleFacade usersModuleFacade;
    private final PaginationValidator paginationValidator;

    @GetMapping
    public ResponseEntity<AuditLogsView> getAuditLogs(
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) OffsetDateTime fromDate,
            @RequestParam(required = false) OffsetDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = new UserId(jwt.getSubject());
        var user = usersModuleFacade.findUserOrThrow(userId);

        var validatedSize = paginationValidator.validatePageSize(size);
        var queryRequest = new AuditLogQueryRequest(entityType, entityId, operation, performedBy, fromDate, toDate);
        var query = queryRequest.toDomain(user.groupId());
        var pageable = PageRequest.of(page, validatedSize);

        var auditLogsPage = auditLogService.findByQuery(query, pageable);
        var auditLogsView = AuditLogsView.from(auditLogsPage);

        return ResponseEntity.ok(auditLogsView);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLogView> getAuditLogById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = new UserId(jwt.getSubject());
        var user = usersModuleFacade.findUserOrThrow(userId);

        var auditLog = auditLogService.findById(AuditLogId.of(id), user.groupId())
                .orElseThrow(() -> new IllegalArgumentException("Audit log not found with id: " + id));
        var auditLogView = AuditLogView.from(auditLog);

        return ResponseEntity.ok(auditLogView);
    }
}
