package pl.btsoftware.backend.transaction;

import lombok.AllArgsConstructor;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.transaction.application.CreateTransactionCommand;
import pl.btsoftware.backend.transaction.application.TransactionService;
import pl.btsoftware.backend.transaction.application.UpdateTransactionCommand;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionId;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
public class TransactionModuleFacade {
    private final TransactionService transactionService;

    public Transaction createTransaction(CreateTransactionCommand command) {
        return transactionService.createTransaction(command);
    }

    public Transaction getTransactionById(UUID id) {
        return transactionService.getTransactionById(TransactionId.of(id));
    }

    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    public List<Transaction> getTransactionsByAccountId(UUID accountId) {
        return transactionService.getTransactionsByAccountId(AccountId.from(accountId));
    }

    public Transaction updateTransaction(UUID transactionId, BigDecimal amount, String description, String category) {
        var command = new UpdateTransactionCommand(TransactionId.of(transactionId), amount, description, category);
        return transactionService.updateTransaction(command);
    }

    public void deleteTransaction(UUID transactionId) {
        transactionService.deleteTransaction(TransactionId.of(transactionId));
    }
}