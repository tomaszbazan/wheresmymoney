package pl.btsoftware.backend.transaction.infrastructure.persistance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, UUID> {
    Optional<TransactionEntity> findByIdAndCreatedByGroupAndIsDeletedFalse(UUID id, UUID createdByGroup);

    Optional<TransactionEntity> findByIdAndCreatedByGroup(UUID id, UUID createdByGroup);

    Page<TransactionEntity> findByCreatedByGroupAndIsDeletedFalse(UUID createdByGroup, Pageable pageable);

    List<TransactionEntity> findByAccountIdAndCreatedByGroupAndIsDeletedFalse(UUID accountId, UUID createdByGroup);

    @Query(
            value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END "
                    + "FROM transaction t "
                    + "WHERE jsonb_path_exists(t.bill, '$.items[*] ? (@.categoryId == $categoryId)', "
                    + "jsonb_build_object('categoryId', CAST(:categoryId AS text))) "
                    + "AND t.created_by_group = :createdByGroup "
                    + "AND t.is_deleted = false",
            nativeQuery = true)
    boolean existsByCategoryIdAndCreatedByGroupAndIsDeletedFalse(
            @Param("categoryId") UUID categoryId, @Param("createdByGroup") UUID createdByGroup);

    boolean existsByAccountIdAndCreatedByGroupAndIsDeletedFalse(UUID categoryId, UUID createdByGroup);

    Optional<TransactionEntity> findByAccountIdAndTransactionHashAndCreatedByGroupAndIsDeletedFalse(
            UUID accountId, String transactionHash, UUID createdByGroup);

    @Query(
            "SELECT t.transactionHash FROM TransactionEntity t WHERE t.accountId = :accountId AND t.transactionHash IN :hashes AND t.createdByGroup = :createdByGroup AND t.isDeleted = false")
    List<String> findExistingHashesByAccountIdAndHashesInAndCreatedByGroup(
            @Param("accountId") UUID accountId,
            @Param("hashes") List<String> hashes,
            @Param("createdByGroup") UUID createdByGroup);
}
