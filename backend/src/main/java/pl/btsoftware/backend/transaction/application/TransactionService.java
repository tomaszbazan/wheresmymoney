package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.category.domain.error.NoCategoriesAvailableException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.DuplicateTransactionException;
import pl.btsoftware.backend.transaction.domain.error.TransactionAlreadyDeletedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountModuleFacade accountModuleFacade;
    private final CategoryQueryFacade categoryQueryFacade;
    private final UsersModuleFacade usersModuleFacade;
    private final AuditModuleFacade auditModuleFacade;

    @Transactional
    public Transaction createTransaction(CreateTransactionCommand command) {
        var user = usersModuleFacade.findUserOrThrow(command.userId());
        var account = accountModuleFacade.getAccount(command.accountId(), user.groupId());
        validateCurrencyMatch(command.amount().currency(), account.balance().currency());
        validateCategoriesExist(command.type(), user.groupId());

        var auditInfo = AuditInfo.create(command.userId().value(), user.groupId().value());
        var transaction = command.toDomain(auditInfo);

        validateNotDuplicate(transaction.accountId(), transaction.transactionHash(), user.groupId());

        transactionRepository.store(transaction);
        if (transaction.type() == TransactionType.INCOME) {
            accountModuleFacade.deposit(command.accountId(), transaction.amount(), command.userId());
        } else {
            accountModuleFacade.withdraw(command.accountId(), transaction.amount(), command.userId());
        }

        auditModuleFacade.logTransactionCreated(transaction.id(), transaction.description(), command.userId(), user.groupId());
        return transaction;
    }

    private void validateCurrencyMatch(Currency transactionCurrency, Currency accountCurrency) {
        if (!transactionCurrency.equals(accountCurrency)) {
            throw new TransactionCurrencyMismatchException(transactionCurrency, accountCurrency);
        }
    }

    private void validateCategoriesExist(TransactionType type, GroupId groupId) {
        var categoryType = type == TransactionType.INCOME ? CategoryType.INCOME : CategoryType.EXPENSE;
        if (!categoryQueryFacade.hasCategories(categoryType, groupId)) {
            throw new NoCategoriesAvailableException(categoryType);
        }
    }

    private void validateNotDuplicate(AccountId accountId, TransactionHash hash, GroupId groupId) {
        transactionRepository.findByAccountIdAndHash(accountId, hash, groupId).ifPresent(duplicate -> {
            throw new DuplicateTransactionException(hash);
        });
    }

    public Transaction getTransactionById(TransactionId transactionId, GroupId groupId) {
        return transactionRepository.findById(transactionId, groupId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    public Page<Transaction> getAllTransactions(GroupId groupId, Pageable pageable) {
        return transactionRepository.findAll(groupId, pageable);
    }

    public List<Transaction> getTransactionsByAccountId(AccountId accountId, GroupId groupId) {
        return transactionRepository.findByAccountId(accountId, groupId);
    }

    @Transactional
    public Transaction updateTransaction(UpdateTransactionCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var transaction = transactionRepository.findById(command.transactionId(), user.groupId())
                .orElseThrow(() -> new TransactionNotFoundException(command.transactionId()));

        validateCategoriesExist(transaction.type(), user.groupId());

        var updatedTransaction = transaction;

        if (command.amount() != null) {
            // Revert old transaction manually
            if (transaction.type() == TransactionType.INCOME) {
                accountModuleFacade.withdraw(transaction.accountId(), transaction.amount(), userId);
            } else {
                accountModuleFacade.deposit(transaction.accountId(), transaction.amount(), userId);
            }

            // Apply new transaction
            if (transaction.type() == TransactionType.INCOME) {
                accountModuleFacade.deposit(transaction.accountId(), command.amount(), userId);
            } else {
                accountModuleFacade.withdraw(transaction.accountId(), command.amount(), userId);
            }

            updatedTransaction = updatedTransaction.updateAmount(command.amount(), userId);
        }

        if (command.description() != null) {
            updatedTransaction = updatedTransaction.updateDescription(command.description(), userId);
        }

        if (command.categoryId() != null) {
            updatedTransaction = updatedTransaction.updateCategory(command.categoryId(), userId);
        }

        transactionRepository.store(updatedTransaction);
        auditModuleFacade.logTransactionUpdated(command.transactionId(), updatedTransaction.description(), userId, user.groupId());
        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var transaction = transactionRepository.findByIdIncludingDeleted(transactionId, user.groupId())
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transaction.isDeleted()) {
            throw new TransactionAlreadyDeletedException(transactionId);
        }

        var deletedTransaction = transaction.delete();
        if (transaction.type() == TransactionType.INCOME) {
            accountModuleFacade.withdraw(transaction.accountId(), transaction.amount(), userId);
        } else {
            accountModuleFacade.deposit(transaction.accountId(), transaction.amount(), userId);
        }
        transactionRepository.store(deletedTransaction);
        auditModuleFacade.logTransactionDeleted(transactionId, transaction.description(), userId, user.groupId());
    }

    @Transactional
    public BulkCreateResult bulkCreateTransactions(BulkCreateTransactionCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var accountId = command.accountId();
        var transactions = command.transactions();

        if (transactions.isEmpty()) {
            return BulkCreateResult.of(List.of(), 0);
        }

        var account = accountModuleFacade.getAccount(accountId, user.groupId());
        validateCurrencyForAllCommands(transactions, account.balance().currency());

        var auditInfo = AuditInfo.create(userId.value(), user.groupId().value());
        var allTransactions = transactions.stream().map(createTransactionCommand -> {
            validateCategoriesExist(createTransactionCommand.type(), user.groupId());
            return createTransactionCommand.toDomain(auditInfo);
        }).toList();

        var allHashes = allTransactions.stream().map(Transaction::transactionHash).toList();

        var existingHashes = transactionRepository.findExistingHashes(accountId, allHashes, user.groupId());

        var savedIds = new ArrayList<TransactionId>();
        var duplicateCount = 0;

        for (var transaction : allTransactions) {
            if (existingHashes.contains(transaction.transactionHash())) {
                duplicateCount++;
            } else {
                transactionRepository.store(transaction);
                if (transaction.type() == TransactionType.INCOME) {
                    accountModuleFacade.deposit(transaction.accountId(), transaction.amount(), userId);
                } else {
                    accountModuleFacade.withdraw(transaction.accountId(), transaction.amount(), userId);
                }
                savedIds.add(transaction.id());
            }
        }

        return BulkCreateResult.of(savedIds, duplicateCount);
    }

    private void validateCurrencyForAllCommands(List<CreateTransactionCommand> commands, Currency accountCurrency) {
        for (var command : commands) {
            validateCurrencyMatch(command.amount().currency(), accountCurrency);
        }
    }
}
