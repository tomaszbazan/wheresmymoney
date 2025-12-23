package pl.btsoftware.backend.transaction.infrastructure.persistance;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InMemoryTransactionRepository implements TransactionRepository {
    private final HashMap<UUID, Transaction> database = new HashMap<>();

    @Override
    public void store(Transaction transaction) {
        database.put(transaction.id().value(), transaction);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id.value()))
                .filter(transaction -> transaction.ownedBy().equals(groupId))
                .filter(transaction -> !transaction.tombstone().isDeleted());
    }

    @Override
    public Optional<Transaction> findByIdIncludingDeleted(TransactionId id, GroupId groupId) {
        return Optional.ofNullable(database.get(id.value()))
                .filter(transaction -> transaction.ownedBy().equals(groupId));
    }

    @Override
    public List<Transaction> findAll(GroupId groupId) {
        return database.values().stream()
                .filter(transaction -> transaction.ownedBy().equals(groupId))
                .filter(transaction -> !transaction.tombstone().isDeleted())
                .toList();
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId, GroupId groupId) {
        return database.values().stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .filter(transaction -> transaction.ownedBy().equals(groupId))
                .filter(transaction -> !transaction.tombstone().isDeleted())
                .toList();
    }

    @Override
    public boolean existsByCategoryId(CategoryId categoryId, GroupId groupId) {
        return database.values().stream()
                .anyMatch(transaction -> transaction.categoryId().equals(categoryId)
                        && transaction.ownedBy().equals(groupId)
                        && !transaction.tombstone().isDeleted());
    }

    @Override
    public Optional<Transaction> findByAccountIdAndHash(AccountId accountId, TransactionHash hash, GroupId groupId) {
        return database.values().stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .filter(transaction -> transaction.transactionHash().equals(hash))
                .filter(transaction -> transaction.ownedBy().equals(groupId))
                .filter(transaction -> !transaction.tombstone().isDeleted())
                .findFirst();
    }

    @Override
    public List<TransactionHash> findExistingHashes(AccountId accountId, List<TransactionHash> hashes, GroupId groupId) {
        return database.values().stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .filter(transaction -> transaction.ownedBy().equals(groupId))
                .filter(transaction -> !transaction.isDeleted())
                .map(Transaction::transactionHash)
                .filter(hashes::contains)
                .distinct()
                .toList();
    }
}
