package pl.btsoftware.backend.transaction;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

@RequiredArgsConstructor
public class TransactionQueryFacade {
    private final TransactionRepository transactionRepository;

    public boolean hasTransactions(CategoryId categoryId, GroupId groupId) {
        return transactionRepository.existsByCategoryId(categoryId, groupId);
    }

    public boolean hasTransactions(AccountId accountId, GroupId groupId) {
        return transactionRepository.existsByAccountId(accountId, groupId);
    }
}
