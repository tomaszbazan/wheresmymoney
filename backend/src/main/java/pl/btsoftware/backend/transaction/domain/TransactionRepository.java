package pl.btsoftware.backend.transaction.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.users.domain.GroupId;

public interface TransactionRepository {
    void store(Transaction transaction);

    Optional<Transaction> findById(TransactionId id, GroupId groupId);

    Optional<Transaction> findByIdIncludingDeleted(TransactionId id, GroupId groupId);

    Page<Transaction> findAll(GroupId groupId, Pageable pageable);

    boolean existsByCategoryId(CategoryId categoryId, GroupId groupId);

    boolean existsByAccountId(AccountId accountId, GroupId groupId);

    Optional<Transaction> findByAccountIdAndHash(AccountId accountId, TransactionHash hash, GroupId groupId);

    List<TransactionHash> findExistingHashes(AccountId accountId, List<TransactionHash> hashes, GroupId groupId);
}
