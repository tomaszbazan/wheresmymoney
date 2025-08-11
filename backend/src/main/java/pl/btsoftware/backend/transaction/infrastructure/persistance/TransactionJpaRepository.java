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
    
    List<TransactionEntity> findByIsDeletedFalse();
    
    List<TransactionEntity> findByAccountIdAndIsDeletedFalse(UUID accountId);
}