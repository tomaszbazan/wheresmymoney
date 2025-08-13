package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;

import java.util.List;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountModuleFacade accountModuleFacade;

    @Transactional // TODO: Verify if transactional works correctly in integration tests
    public Transaction createTransaction(CreateTransactionCommand command) {
        var account = accountModuleFacade.getAccount(command.accountId());
        validateDescriptionLength(command.description());
        validateCurrencyMatch(command.currency(), account.balance().currency());

        var transaction = command.toDomain();
        transactionRepository.store(transaction);
        accountModuleFacade.addTransaction(command.accountId(), transaction.id(), transaction.amount(), transaction.type());

        return transaction;
    }

    private void validateDescriptionLength(String description) {
        if (description == null || description.trim().isEmpty() || description.length() > 200) {
            throw new IllegalArgumentException("Description must be between 1 and 200 characters");
        }
    }

    private void validateCurrencyMatch(Currency transactionCurrency, Currency accountCurrency) {
        if (!transactionCurrency.equals(accountCurrency)) {
            throw new IllegalArgumentException("Transaction currency must match account currency");
        }
    }

    public Transaction getTransactionById(TransactionId transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public List<Transaction> getTransactionsByAccountId(AccountId accountId) {
        return transactionRepository.findByAccountId(accountId);
    }

    @Transactional
    public Transaction updateTransaction(UpdateTransactionCommand command) {
        var originalTransaction = transactionRepository.findById(command.transactionId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        var updatedTransaction = originalTransaction;

        if (command.amount() != null) {
            accountModuleFacade.changeTransaction(originalTransaction.accountId(), originalTransaction.id(), updatedTransaction.amount(), Money.of(command.amount(), command.currency()), originalTransaction.type());
            updatedTransaction = updatedTransaction.updateAmount(command.amount());
        }
        
        if (command.description() != null) {
            updatedTransaction = updatedTransaction.updateDescription(command.description());
        }
        
        if (command.category() != null) {
            updatedTransaction = updatedTransaction.updateCategory(command.category());
        }

        transactionRepository.store(updatedTransaction);
        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId) {
        var transaction = transactionRepository.findByIdIncludingDeleted(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
        
        if (transaction.tombstone().isDeleted()) {
            throw new IllegalArgumentException("Transaction not found");
        }

        var deletedTransaction = transaction.delete();
        transactionRepository.store(deletedTransaction);

        accountModuleFacade.removeTransaction(transaction.accountId(), transactionId, transaction.amount(), transaction.type());
    }
}