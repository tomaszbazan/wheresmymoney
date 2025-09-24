package pl.btsoftware.backend.transaction.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Profile("!test")
public class JpaTransactionRepository implements TransactionRepository {

    private final TransactionJpaRepository repository;

    @Override
    public void store(Transaction transaction) {
        TransactionEntity entity = TransactionEntity.fromDomain(transaction);
        repository.save(entity);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id, GroupId groupId) {
        return repository.findByIdAndCreatedByGroupAndIsDeletedFalse(id.value(), groupId.value())
                .map(TransactionEntity::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdIncludingDeleted(TransactionId id, GroupId groupId) {
        return repository.findByIdAndCreatedByGroup(id.value(), groupId.value())
                .map(TransactionEntity::toDomain);
    }

    @Override
    public List<Transaction> findAll(GroupId groupId) {
        return repository.findByCreatedByGroupAndIsDeletedFalse(groupId.value()).stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId, GroupId groupId) {
        return repository.findByAccountIdAndCreatedByGroupAndIsDeletedFalse(accountId.value(), groupId.value()).stream()
                .map(TransactionEntity::toDomain)
                .toList();
    }
}
