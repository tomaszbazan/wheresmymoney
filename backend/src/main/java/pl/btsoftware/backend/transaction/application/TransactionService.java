package pl.btsoftware.backend.transaction.application;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.btsoftware.backend.account.AccountModuleFacade;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.audit.AuditModuleFacade;
import pl.btsoftware.backend.category.CategoryQueryFacade;
import pl.btsoftware.backend.category.domain.error.CategoryNotFoundException;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.transaction.domain.error.TransactionAlreadyDeletedException;
import pl.btsoftware.backend.transaction.domain.error.TransactionCurrencyMismatchException;
import pl.btsoftware.backend.transaction.domain.error.TransactionNotFoundException;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

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

        var auditInfo = AuditInfo.create(command.userId(), user.groupId());
        var transaction = command.toDomain(auditInfo, account.balance().currency());

        validateCurrencyMatch(transaction.amount().currency(), account.balance().currency());

        validateCategoriesExist(transaction.bill().categories(), user.groupId());

        transactionRepository.store(transaction);

        applyTransactionToAccount(transaction, command.userId());

        auditModuleFacade.logTransactionCreated(
                transaction.id(), transaction.description(), command.userId(), user.groupId());
        return transaction;
    }

    public Transaction getTransactionById(TransactionId transactionId, GroupId groupId) {
        return transactionRepository
                .findById(transactionId, groupId)
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }

    public Page<Transaction> getAllTransactions(GroupId groupId, Pageable pageable) {
        return transactionRepository.findAll(groupId, pageable);
    }

    @Transactional
    public Transaction updateTransaction(UpdateTransactionCommand command, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var oldTransaction = getTransactionById(command.transactionId(), user.groupId());

        var newAccountId = command.accountId();
        var newAccount = accountModuleFacade.getAccount(newAccountId, user.groupId());

        validateCurrencyMatch(
                oldTransaction.amount().currency(), newAccount.balance().currency());

        var bill = command.bill().toDomain(newAccount.balance().currency());

        var categoryIds = bill.categories();
        validateCategoriesExist(categoryIds, user.groupId());

        var updatedTransaction = oldTransaction.updateBill(bill, newAccountId, command.transactionDate(), userId);
        transactionRepository.store(updatedTransaction);

        revertTransactionFromAccount(oldTransaction, userId);
        applyTransactionToAccount(updatedTransaction, userId);

        auditModuleFacade.logTransactionUpdated(
                command.transactionId(), updatedTransaction.description(), userId, user.groupId());
        return updatedTransaction;
    }

    @Transactional
    public void deleteTransaction(TransactionId transactionId, UserId userId) {
        var user = usersModuleFacade.findUserOrThrow(userId);
        var transaction = transactionRepository
                .findByIdIncludingDeleted(transactionId, user.groupId())
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));

        if (transaction.isDeleted()) {
            throw new TransactionAlreadyDeletedException(transactionId);
        }

        var deletedTransaction = transaction.delete();
        revertTransactionFromAccount(transaction, userId);
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

        var auditInfo = AuditInfo.create(userId.value(), user.groupId().value());
        var allTransactions = transactions.stream()
                .map(createTransactionCommand -> {
                    var categoryIds = createTransactionCommand.billCommand().billItems().stream()
                            .map(BillItemCommand::categoryId)
                            .collect(Collectors.toSet());
                    validateCategoriesExist(categoryIds, user.groupId());
                    return createTransactionCommand.toDomain(
                            auditInfo, account.balance().currency());
                })
                .toList();

        var allHashes =
                allTransactions.stream().map(Transaction::transactionHash).toList();

        var existingHashes = transactionRepository.findExistingHashes(accountId, allHashes, user.groupId());

        var savedIds = new ArrayList<TransactionId>();
        var duplicateCount = 0;

        for (var transaction : allTransactions) {
            if (existingHashes.contains(transaction.transactionHash())) {
                duplicateCount++;
            } else {
                transactionRepository.store(transaction);
                applyTransactionToAccount(transaction, userId);
                savedIds.add(transaction.id());
            }
        }

        return BulkCreateResult.of(savedIds, duplicateCount);
    }

    private void validateCurrencyMatch(Currency transactionCurrency, Currency accountCurrency) {
        if (!transactionCurrency.equals(accountCurrency)) {
            throw new TransactionCurrencyMismatchException(transactionCurrency, accountCurrency);
        }
    }

    private void validateCategoriesExist(Set<CategoryId> categoryIds, GroupId groupId) {
        if (!categoryQueryFacade.allCategoriesExists(categoryIds, groupId)) {
            throw new CategoryNotFoundException();
        }
    }

    private void applyTransactionToAccount(Transaction transaction, UserId userId) {
        if (transaction.type() == TransactionType.INCOME) {
            accountModuleFacade.deposit(transaction.accountId(), transaction.amount(), userId);
        } else {
            accountModuleFacade.withdraw(transaction.accountId(), transaction.amount(), userId);
        }
    }

    private void revertTransactionFromAccount(Transaction transaction, UserId userId) {
        if (transaction.type() == TransactionType.INCOME) {
            accountModuleFacade.withdraw(transaction.accountId(), transaction.amount(), userId);
        } else {
            accountModuleFacade.deposit(transaction.accountId(), transaction.amount(), userId);
        }
    }
}
