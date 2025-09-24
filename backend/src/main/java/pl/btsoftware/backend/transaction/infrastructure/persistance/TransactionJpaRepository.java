package pl.btsoftware.backend.transaction.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByAccountId(UUID accountId);

    Optional<TransactionEntity> findByIdAndIsDeletedFalse(UUID id);
    Optional<TransactionEntity> findByIdAndCreatedByGroupAndIsDeletedFalse(UUID id, UUID createdByGroup);
    Optional<TransactionEntity> findByIdAndCreatedByGroup(UUID id, UUID createdByGroup);

    List<TransactionEntity> findByIsDeletedFalse();
    List<TransactionEntity> findByCreatedByGroupAndIsDeletedFalse(UUID createdByGroup);
    List<TransactionEntity> findByAccountIdAndIsDeletedFalse(UUID accountId);
    List<TransactionEntity> findByAccountIdAndCreatedByGroupAndIsDeletedFalse(UUID accountId, UUID createdByGroup);
}
