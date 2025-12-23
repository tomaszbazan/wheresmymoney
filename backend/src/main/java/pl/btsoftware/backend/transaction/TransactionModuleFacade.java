package pl.btsoftware.backend.transaction;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.application.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class TransactionModuleFacade {
    private final TransactionService transactionService;
    private final UsersModuleFacade usersModuleFacade;
    private final TransactionRepository transactionRepository;

    public Transaction createTransaction(CreateTransactionCommand command) {
        return transactionService.createTransaction(command);
    }

    public Transaction getTransactionById(UUID id, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getTransactionById(TransactionId.of(id), user.groupId());
    }

    public List<Transaction> getAllTransactions(UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getAllTransactions(user.groupId());
    }

    public List<Transaction> getTransactionsByAccountId(UUID accountId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        return transactionService.getTransactionsByAccountId(AccountId.from(accountId), user.groupId());
    }

    public Transaction updateTransaction(UpdateTransactionCommand command, UserId userId) {
        return transactionService.updateTransaction(command, userId);
    }

    public void deleteTransaction(UUID transactionId, UserId userId) {
        transactionService.deleteTransaction(TransactionId.of(transactionId), userId);
    }

    public BulkCreateResult bulkCreateTransactions(BulkCreateTransactionCommand command, UserId userId) {
        return transactionService.bulkCreateTransactions(command, userId);
    }
}
