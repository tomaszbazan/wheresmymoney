package pl.btsoftware.backend.transaction.infrastructure.persistance;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;

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
    public Optional<Transaction> findById(TransactionId id) {
        return Optional.ofNullable(database.get(id.value()))
                .filter(transaction -> !transaction.tombstone().isDeleted());
    }

    @Override
    public Optional<Transaction> findByIdIncludingDeleted(TransactionId id) {
        return Optional.ofNullable(database.get(id.value()));
    }

    @Override
    public List<Transaction> findAll() {
        return database.values().stream()
                .filter(transaction -> !transaction.tombstone().isDeleted())
                .toList();
    }

    @Override
    public List<Transaction> findByAccountId(AccountId accountId) {
        return database.values().stream()
                .filter(transaction -> transaction.accountId().equals(accountId))
                .filter(transaction -> !transaction.tombstone().isDeleted())
                .toList();
    }
}