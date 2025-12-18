package pl.btsoftware.backend.transaction;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;

@RequiredArgsConstructor
public class TransactionQueryFacade {
    private final TransactionRepository transactionRepository;

    public boolean categoryHasTransactions(CategoryId categoryId, GroupId groupId) {
        return transactionRepository.existsByCategoryId(categoryId, groupId);
    }
}
