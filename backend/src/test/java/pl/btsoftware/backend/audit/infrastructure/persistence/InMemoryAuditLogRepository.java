package pl.btsoftware.backend.audit.infrastructure.persistence;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.audit.domain.AuditLog;
import pl.btsoftware.backend.audit.domain.AuditLogId;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditLogRepository;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@Profile("test")
public class InMemoryAuditLogRepository implements AuditLogRepository {
    private final HashMap<AuditLogId, AuditLog> database = new HashMap<>();

    @Override
    public void store(AuditLog auditLog) {
        database.put(auditLog.id(), auditLog);
    }

    @Override
    public Optional<AuditLog> findById(AuditLogId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id)).filter(log -> log.groupId().equals(groupId));
    }

    @Override
    public Page<AuditLog> findByQuery(AuditLogQuery query, Pageable pageable) {
        var filteredLogs =
                database.values().stream()
                        .filter(log -> log.groupId().equals(query.groupId()))
                        .filter(
                                log ->
                                        query.entityType() == null
                                                || log.entityType().equals(query.entityType()))
                        .filter(
                                log ->
                                        query.entityId() == null
                                                || log.entityId().equals(query.entityId()))
                        .filter(
                                log ->
                                        query.operation() == null
                                                || log.operation().equals(query.operation()))
                        .filter(
                                log ->
                                        query.performedBy() == null
                                                || log.performedBy().equals(query.performedBy()))
                        .filter(
                                log ->
                                        query.fromDate() == null
                                                || !log.performedAt().isBefore(query.fromDate()))
                        .filter(
                                log ->
                                        query.toDate() == null
                                                || !log.performedAt().isAfter(query.toDate()))
                        .sorted(Comparator.comparing(AuditLog::performedAt).reversed())
                        .toList();

        var totalElements = filteredLogs.size();
        var start = (int) pageable.getOffset();
        var end = Math.min(start + pageable.getPageSize(), totalElements);

        List<AuditLog> pageContent =
                (start >= totalElements) ? List.of() : filteredLogs.subList(start, end);

        return new PageImpl<>(pageContent, pageable, totalElements);
    }
}
