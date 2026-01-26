package pl.btsoftware.backend.transaction;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

@RequiredArgsConstructor
public class TransactionModuleFacade {
    private final TransactionService transactionService;
    private final UsersModuleFacade usersModuleFacade;

    public Transaction createTransaction(CreateTransactionCommand command) {
        return transactionService.createTransaction(command);
    }

    public Transaction getTransactionById(UUID id, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getTransactionById(TransactionId.of(id), user.groupId());
    }

    public Page<Transaction> getAllTransactions(UserId userId, Pageable pageable) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getAllTransactions(user.groupId(), pageable);
    }

    public List<Transaction> getTransactionsByAccountId(UUID accountId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getTransactionsByAccountId(
                AccountId.from(accountId), user.groupId());
    }

    public Transaction updateTransaction(UpdateTransactionCommand command, UserId userId) {
        return transactionService.updateTransaction(command, userId);
    }

    public void deleteTransaction(UUID transactionId, UserId userId) {
        transactionService.deleteTransaction(TransactionId.of(transactionId), userId);
    }

    public BulkCreateResult bulkCreateTransactions(
            BulkCreateTransactionCommand command, UserId userId) {
        return transactionService.bulkCreateTransactions(command, userId);
    }
}
