package pl.btsoftware.backend.audit.domain;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.users.domain.GroupId;

public interface AuditLogRepository {
    void store(AuditLog auditLog);

    Optional<AuditLog> findById(AuditLogId id, GroupId groupId);

    Page<AuditLog> findByQuery(AuditLogQuery query, Pageable pageable);
}
