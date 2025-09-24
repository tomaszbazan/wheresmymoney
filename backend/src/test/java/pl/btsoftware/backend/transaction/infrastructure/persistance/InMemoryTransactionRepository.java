package pl.btsoftware.backend.transaction.infrastructure.persistance;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
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
}
