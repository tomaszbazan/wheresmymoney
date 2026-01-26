package pl.btsoftware.backend.audit.application;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.audit.domain.*;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.Optional;

import static pl.btsoftware.backend.audit.domain.AuditOperation.*;

@Service
@AllArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLog logCreate(AuditEntityType entityType, EntityId entityId, UserId performedBy, GroupId groupId, String changeDescription) {
        var auditLog = AuditLog.create(CREATE, entityType, entityId, performedBy, groupId, changeDescription);
        auditLogRepository.store(auditLog);
        return auditLog;
    }

    public AuditLog logUpdate(AuditEntityType entityType, EntityId entityId, UserId performedBy, GroupId groupId, String changeDescription) {
        var auditLog = AuditLog.create(UPDATE, entityType, entityId, performedBy, groupId, changeDescription);
        auditLogRepository.store(auditLog);
        return auditLog;
    }

    public AuditLog logDelete(AuditEntityType entityType, EntityId entityId, UserId performedBy, GroupId groupId, String changeDescription) {
        var auditLog = AuditLog.create(DELETE, entityType, entityId, performedBy, groupId, changeDescription);
        auditLogRepository.store(auditLog);
        return auditLog;
    }

    public Optional<AuditLog> findById(AuditLogId id, GroupId groupId) {
        return auditLogRepository.findById(id, groupId);
    }

    public Page<AuditLog> findByQuery(AuditLogQuery query, Pageable pageable) {
        return auditLogRepository.findByQuery(query, pageable);
    }
}
