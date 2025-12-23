package pl.btsoftware.backend.transaction.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByIdAndCreatedByGroupAndIsDeletedFalse(UUID id, UUID createdByGroup);
    Optional<TransactionEntity> findByIdAndCreatedByGroup(UUID id, UUID createdByGroup);
    List<TransactionEntity> findByCreatedByGroupAndIsDeletedFalse(UUID createdByGroup);
    List<TransactionEntity> findByAccountIdAndCreatedByGroupAndIsDeletedFalse(UUID accountId, UUID createdByGroup);
    boolean existsByCategoryIdAndCreatedByGroupAndIsDeletedFalse(UUID categoryId, UUID createdByGroup);

    Optional<TransactionEntity> findByAccountIdAndTransactionHashAndCreatedByGroupAndIsDeletedFalse(UUID accountId, String transactionHash, UUID createdByGroup);

    @Query("SELECT t.transactionHash FROM TransactionEntity t WHERE t.accountId = :accountId AND t.transactionHash IN :hashes AND t.createdByGroup = :createdByGroup AND t.isDeleted = false")
    List<String> findExistingHashesByAccountIdAndHashesInAndCreatedByGroup(@Param("accountId") UUID accountId, @Param("hashes") List<String> hashes, @Param("createdByGroup") UUID createdByGroup);
}
