package pl.btsoftware.backend.transaction.infrastructure.persistance;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
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
        return repository.findByIdAndCreatedByGroupAndIsDeletedFalse(id.value(), groupId.value()).map(TransactionEntity::toDomain);
    }

    @Override
    public Optional<Transaction> findByIdIncludingDeleted(TransactionId id, GroupId groupId) {
        return repository.findByIdAndCreatedByGroup(id.value(), groupId.value()).map(TransactionEntity::toDomain);
    }

    @Override
    public Page<Transaction> findAll(GroupId groupId, Pageable pageable) {
        return repository.findByCreatedByGroupAndIsDeletedFalse(groupId.value(), pageable).map(TransactionEntity::toDomain);
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId, GroupId groupId) {
        return repository.findByAccountIdAndCreatedByGroupAndIsDeletedFalse(accountId.value(), groupId.value()).stream().map(TransactionEntity::toDomain).toList();
    }

    @Override
    public boolean existsByCategoryId(CategoryId categoryId, GroupId groupId) {
        return repository.existsByCategoryIdAndCreatedByGroupAndIsDeletedFalse(categoryId.value(), groupId.value());
    }

    @Override
    public boolean existsByAccountId(AccountId accountId, GroupId groupId) {
        return repository.existsByAccountIdAndCreatedByGroupAndIsDeletedFalse(accountId.value(), groupId.value());
    }

    @Override
    public Optional<Transaction> findByAccountIdAndHash(AccountId accountId, TransactionHash hash, GroupId groupId) {
        return repository.findByAccountIdAndTransactionHashAndCreatedByGroupAndIsDeletedFalse(accountId.value(), hash.value(), groupId.value()).map(TransactionEntity::toDomain);
    }

    @Override
    public List<TransactionHash> findExistingHashes(AccountId accountId, List<TransactionHash> hashes, GroupId groupId) {
        var hashValues = hashes.stream().map(TransactionHash::value).toList();

        return repository.findExistingHashesByAccountIdAndHashesInAndCreatedByGroup(accountId.value(), hashValues, groupId.value()).stream().map(TransactionHash::new).toList();
    }
}
