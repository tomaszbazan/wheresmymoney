package pl.btsoftware.backend.audit.infrastructure.persistence;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.audit.domain.AuditLog;
import pl.btsoftware.backend.audit.domain.AuditLogId;
import pl.btsoftware.backend.audit.domain.AuditLogQuery;
import pl.btsoftware.backend.audit.domain.AuditLogRepository;
import pl.btsoftware.backend.users.domain.GroupId;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaAuditLogRepository implements AuditLogRepository {

    private final AuditLogJpaRepository repository;

    @Override
    public void store(AuditLog auditLog) {
        var entity = AuditLogEntity.fromDomain(auditLog);
        repository.save(entity);
    }

    @Override
    public Optional<AuditLog> findById(AuditLogId id, GroupId groupId) {
        return repository
                .findByIdAndGroupId(id.value(), groupId.value())
                .map(AuditLogEntity::toDomain);
    }

    @Override
    public Page<AuditLog> findByQuery(AuditLogQuery query, Pageable pageable) {
        Specification<AuditLogEntity> spec =
                (root, criteriaQuery, criteriaBuilder) -> {
                    var predicates = new ArrayList<Predicate>();

                    predicates.add(
                            criteriaBuilder.equal(root.get("groupId"), query.groupId().value()));

                    if (query.entityType() != null) {
                        predicates.add(
                                criteriaBuilder.equal(root.get("entityType"), query.entityType()));
                    }

                    if (query.entityId() != null) {
                        predicates.add(
                                criteriaBuilder.equal(
                                        root.get("entityId"), query.entityId().value()));
                    }

                    if (query.operation() != null) {
                        predicates.add(
                                criteriaBuilder.equal(root.get("operation"), query.operation()));
                    }

                    if (query.performedBy() != null) {
                        predicates.add(
                                criteriaBuilder.equal(
                                        root.get("performedBy"), query.performedBy().value()));
                    }

                    if (query.fromDate() != null) {
                        predicates.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                        root.get("performedAt"), query.fromDate()));
                    }

                    if (query.toDate() != null) {
                        predicates.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                        root.get("performedAt"), query.toDate()));
                    }

                    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("performedAt")));

                    return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
                };

        return repository.findAll(spec, pageable).map(AuditLogEntity::toDomain);
    }
}
