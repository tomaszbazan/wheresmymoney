package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.account.domain.AccountId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void store(Transaction transaction);

    Optional<Transaction> findById(TransactionId id);

    Optional<Transaction> findByIdIncludingDeleted(TransactionId id);

    List<Transaction> findAll();

    List<Transaction> findByAccountId(AccountId accountId);
}