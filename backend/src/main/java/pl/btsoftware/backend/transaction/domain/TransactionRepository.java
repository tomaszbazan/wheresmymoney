package pl.btsoftware.backend.transaction.domain;

import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.users.domain.GroupId;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository {
    void store(Transaction transaction);
    Optional<Transaction> findById(TransactionId id, GroupId groupId);
    Optional<Transaction> findByIdIncludingDeleted(TransactionId id, GroupId groupId);
    List<Transaction> findAll(GroupId groupId);
    List<Transaction> findByAccountId(AccountId accountId, GroupId groupId);
    boolean existsByCategoryId(CategoryId categoryId, GroupId groupId);
    Optional<Transaction> findByAccountIdAndHash(AccountId accountId, TransactionHash hash, GroupId groupId);

    List<TransactionHash> findExistingHashes(AccountId accountId, List<TransactionHash> hashes, GroupId groupId);
}
