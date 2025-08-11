package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionId;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.TransactionType;

import java.util.List;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountModuleFacade accountModuleFacade;

    @Transactional // TODO: Verify if transactional works correctly in integration tests
    public Transaction createTransaction(CreateTransactionCommand command) {
        var account = accountModuleFacade.getAccount(command.accountId().value());
        validateDescriptionLength(command.description());
        validateCurrencyMatch(command.currency(), account.balance().currency());

        var transaction = command.toDomain();
        transactionRepository.store(transaction);
        accountModuleFacade.addTransaction(command.accountId().value(), command.amount(), command.type().name());

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
            var oldAmount = originalTransaction.type() == TransactionType.INCOME ? 
                    originalTransaction.amount().amount() : originalTransaction.amount().amount().negate();
            var newAmount = originalTransaction.type() == TransactionType.INCOME ? 
                    command.amount() : command.amount().negate();
            var balanceAdjustment = newAmount.subtract(oldAmount);
            
            accountModuleFacade.addTransaction(originalTransaction.accountId().value(), balanceAdjustment, TransactionType.INCOME.name());
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
        
        var reverseAmount = transaction.type() == TransactionType.EXPENSE ? 
                transaction.amount().amount() : transaction.amount().amount().negate();
        accountModuleFacade.addTransaction(transaction.accountId().value(), reverseAmount, 
                transaction.type() == TransactionType.EXPENSE ? TransactionType.INCOME.name() : TransactionType.EXPENSE.name());
    }
}