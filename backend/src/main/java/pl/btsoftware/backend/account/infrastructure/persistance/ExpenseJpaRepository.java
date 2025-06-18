package pl.btsoftware.backend.account.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, UUID> {
    List<ExpenseEntity> findByAccountId(UUID accountId);
}
