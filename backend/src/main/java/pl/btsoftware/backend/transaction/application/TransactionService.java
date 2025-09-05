package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionId;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.TransactionAccessDeniedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionAlreadyDeletedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionDescriptionTooLongException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountModuleFacade accountModuleFacade;
    private final UsersModuleFacade usersModuleFacade;

    @Transactional // TODO: Verify if transactional works correctly in integration tests
    public Transaction createTransaction(CreateTransactionCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());
        var account = accountModuleFacade.getAccount(command.accountId(), new GroupId(user.groupId()));
        validateDescriptionLength(command.description());
        validateCurrencyMatch(command.amount().currency(), account.balance().currency());

        var auditInfo = AuditInfo.create(command.userId().value(), user.groupId());
        var transaction = command.toDomain(auditInfo);
        transactionRepository.store(transaction);
        accountModuleFacade.addTransaction(command.accountId(), transaction.id(), transaction.amount(), transaction.type(), command.userId());

        return transaction;
    }

    private void validateDescriptionLength(String description) {
        if (description != null && description.length() > 200) {
            throw new TransactionDescriptionTooLongException();
        }
    }

    private void validateCurrencyMatch(Currency transactionCurrency, Currency accountCurrency) {
        if (!transactionCurrency.equals(accountCurrency)) {
            throw new TransactionCurrencyMismatchException(transactionCurrency, accountCurrency);
        }
    }

    public Transaction getTransactionById(TransactionId transactionId, GroupId groupId) {
        return transactionRepository.findById(transactionId, groupId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    public List<Transaction> getAllTransactions(GroupId groupId) {
        return transactionRepository.findAll(groupId);
    }

    public List<Transaction> getTransactionsByAccountId(AccountId accountId, GroupId groupId) {
        return transactionRepository.findByAccountId(accountId, groupId);
    }

    @Transactional
    public Transaction updateTransaction(UpdateTransactionCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var transaction = transactionRepository.findById(command.transactionId(), new GroupId(user.groupId()))
                .orElseThrow(() -> new TransactionNotFoundException(command.transactionId()));
        if (!transaction.ownedBy().equals(new GroupId(user.groupId()))) {
            throw new TransactionAccessDeniedException();
        }

        var updatedTransaction = transaction;

        if (command.amount() != null) {
            accountModuleFacade.changeTransaction(transaction.accountId(), transaction.id(), updatedTransaction.amount(), command.amount(), transaction.type(), userId);
            updatedTransaction = updatedTransaction.updateAmount(command.amount(), userId);
        }
        
        if (command.description() != null) {
            updatedTransaction = updatedTransaction.updateDescription(command.description(), userId);
        }
        
        if (command.category() != null) {
            updatedTransaction = updatedTransaction.updateCategory(command.category(), userId);
        }

        transactionRepository.store(updatedTransaction);
        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var transaction = transactionRepository.findByIdIncludingDeleted(transactionId, new GroupId(user.groupId()))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
        
        if (transaction.isDeleted()) {
            throw new TransactionAlreadyDeletedException(transactionId);
        }

        var deletedTransaction = transaction.delete();
        accountModuleFacade.removeTransaction(transaction.accountId(), transactionId, transaction.amount(), transaction.type(), userId);
        transactionRepository.store(deletedTransaction);

    }
}