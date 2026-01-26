package pl.btsoftware.backend.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.Optional;

public interface AuditLogRepository {
    void store(AuditLog auditLog);

    Optional<AuditLog> findById(AuditLogId id, GroupId groupId);

    Page<AuditLog> findByQuery(AuditLogQuery query, Pageable pageable);
}
