package pl.btsoftware.backend.migration.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountRepository;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.migration.api.SyncAccountRequest;
import pl.btsoftware.backend.migration.api.SyncCategoryRequest;
import pl.btsoftware.backend.migration.api.SyncResultResponse;
import pl.btsoftware.backend.migration.api.SyncTransactionRequest;
import pl.btsoftware.backend.migration.domain.MigrationEntityMapping;
import pl.btsoftware.backend.migration.domain.MigrationEntityMappingRepository;
import pl.btsoftware.backend.migration.domain.MigrationSyncLog;
import pl.btsoftware.backend.migration.domain.MigrationSyncLogRepository;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;
import pl.btsoftware.backend.transaction.domain.TransactionHashCalculator;
import pl.btsoftware.backend.transaction.domain.TransactionRepository;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationSyncService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final MigrationEntityMappingRepository mappingRepository;
    private final MigrationSyncLogRepository syncLogRepository;
    private final TransactionHashCalculator hashCalculator = new TransactionHashCalculator();

    @Transactional
    public SyncResultResponse syncTransaction(SyncTransactionRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            if (mappingRepository.existsByEntityTypeAndOldId("TRANSACTION_" + request.type(), request.oldId())) {
                log.info("Transaction {} already synced, skipping", request.oldId());
                var mapping = mappingRepository.findByEntityTypeAndOldId("TRANSACTION_" + request.type(), request.oldId()).orElseThrow();
                logSync("TRANSACTION_" + request.type(), "SKIP", request.oldId(), mapping.getNewId(), "SUCCESS", null, startTime);
                return new SyncResultResponse(true, mapping.getNewId(), "Already synced", request.oldId());
            }

            UUID accountId = resolveAccountId(request.oldAccountId());
            UUID categoryId = resolveCategoryId(request.oldCategoryId(), request.type());
            UserId userId = resolveUserId(request.oldUserId());
            GroupId groupId = resolveGroupId(request.oldGroupId());

            if (accountId == null || categoryId == null) {
                String error = "Missing mapping for account or category";
                logSync("TRANSACTION_" + request.type(), "INSERT", request.oldId(), null, "ERROR", error, startTime);
                return new SyncResultResponse(false, null, error, request.oldId());
            }

            Money amount = Money.of(request.amount(), mapCurrency(request.currencyId()));
            TransactionType type = TransactionType.valueOf(request.type());
            LocalDate transactionDate = request.date() != null ? request.date().toLocalDate() : LocalDate.now();

            AuditInfo auditInfo = AuditInfo.create(userId, groupId);

            TransactionHash hash = hashCalculator.calculateHash(
                    new AccountId(accountId),
                    amount,
                    request.comment(),
                    transactionDate,
                    type
            );

            Transaction transaction = Transaction.create(
                    new AccountId(accountId),
                    amount,
                    request.comment(),
                    type,
                    new CategoryId(categoryId),
                    transactionDate,
                    hash,
                    auditInfo
            );

            transactionRepository.store(transaction);

            MigrationEntityMapping mapping = new MigrationEntityMapping(
                    "TRANSACTION_" + request.type(),
                    request.oldId(),
                    null,
                    transaction.id().value()
            );
            mappingRepository.save(mapping);

            logSync("TRANSACTION_" + request.type(), "INSERT", request.oldId(), transaction.id().value(), "SUCCESS", null, startTime);

            log.info("Synced transaction {} -> {}", request.oldId(), transaction.id().value());
            return new SyncResultResponse(true, transaction.id().value(), "Transaction synced successfully", request.oldId());

        } catch (Exception e) {
            log.error("Failed to sync transaction {}", request.oldId(), e);
            logSync("TRANSACTION_" + request.type(), "INSERT", request.oldId(), null, "ERROR", e.getMessage(), startTime);
            return new SyncResultResponse(false, null, "Error: " + e.getMessage(), request.oldId());
        }
    }

    @Transactional
    public SyncResultResponse syncAccount(SyncAccountRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            Optional<MigrationEntityMapping> existingMapping =
                    mappingRepository.findByEntityTypeAndOldId("ACCOUNT", request.oldId());

            if (existingMapping.isPresent()) {
                UUID accountId = existingMapping.get().getNewId();
                GroupId groupId = resolveGroupId(request.oldGroupId());
                Optional<Account> accountOpt = accountRepository.findById(new AccountId(accountId), groupId);

                if (accountOpt.isPresent()) {
                    Account account = accountOpt.get();
                    Money newBalance = Money.of(request.amount(), mapCurrency(request.currencyId()));

                    if (!account.balance().equals(newBalance)) {
                        Money difference = newBalance.subtract(account.balance());
                        Account updatedAccount = difference.value().compareTo(BigDecimal.ZERO) > 0
                                ? account.deposit(difference)
                                : account.withdraw(difference.negate());
                        accountRepository.store(updatedAccount);

                        logSync("ACCOUNT", "UPDATE", request.oldId(), accountId, "SUCCESS", null, startTime);
                        log.info("Updated account balance {} -> {}", request.oldId(), accountId);
                        return new SyncResultResponse(true, accountId, "Account balance updated", request.oldId());
                    } else {
                        logSync("ACCOUNT", "SKIP", request.oldId(), accountId, "SKIPPED", "Balance unchanged", startTime);
                        return new SyncResultResponse(true, accountId, "Account unchanged", request.oldId());
                    }
                }
            }

            UserId userId = resolveUserId(request.oldUserId());
            GroupId groupId = resolveGroupId(request.oldGroupId());
            Money balance = Money.of(request.amount(), mapCurrency(request.currencyId()));
            AuditInfo auditInfo = AuditInfo.create(userId, groupId);

            Account account = new Account(
                    AccountId.generate(),
                    request.accountName(),
                    balance,
                    auditInfo
            );

            accountRepository.store(account);

            MigrationEntityMapping mapping = new MigrationEntityMapping(
                    "ACCOUNT",
                    request.oldId(),
                    request.accountName(),
                    account.id().value()
            );
            mappingRepository.save(mapping);

            logSync("ACCOUNT", "INSERT", request.oldId(), account.id().value(), "SUCCESS", null, startTime);

            log.info("Synced account {} -> {}", request.oldId(), account.id().value());
            return new SyncResultResponse(true, account.id().value(), "Account synced successfully", request.oldId());

        } catch (Exception e) {
            log.error("Failed to sync account {}", request.oldId(), e);
            logSync("ACCOUNT", "INSERT", request.oldId(), null, "ERROR", e.getMessage(), startTime);
            return new SyncResultResponse(false, null, "Error: " + e.getMessage(), request.oldId());
        }
    }

    @Transactional
    public SyncResultResponse syncCategory(SyncCategoryRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            String entityType = "CATEGORY_" + request.type();

            if (mappingRepository.existsByEntityTypeAndOldId(entityType, request.oldId())) {
                log.info("Category {} already synced, skipping", request.oldId());
                var mapping = mappingRepository.findByEntityTypeAndOldId(entityType, request.oldId()).orElseThrow();
                logSync(entityType, "SKIP", request.oldId(), mapping.getNewId(), "SUCCESS", null, startTime);
                return new SyncResultResponse(true, mapping.getNewId(), "Already synced", request.oldId());
            }

            UserId userId = resolveUserId(request.oldUserId());
            GroupId groupId = resolveGroupId(request.oldGroupId());
            CategoryType categoryType = CategoryType.valueOf(request.type());
            Color color = request.icon() != null && request.icon().matches("^#[0-9A-Fa-f]{6}$")
                    ? new Color(request.icon())
                    : new Color("#808080");

            CategoryId parentId = null;
            if (request.oldParentCategoryId() != null) {
                UUID parentUuid = resolveCategoryId(request.oldParentCategoryId(), request.type());
                if (parentUuid != null) {
                    parentId = new CategoryId(parentUuid);
                }
            }

            AuditInfo auditInfo = AuditInfo.create(userId, groupId);

            Category category = Category.create(
                    request.categoryName(),
                    categoryType,
                    color,
                    parentId,
                    auditInfo
            );

            categoryRepository.store(category);

            MigrationEntityMapping mapping = new MigrationEntityMapping(
                    entityType,
                    request.oldId(),
                    request.categoryName(),
                    category.id().value()
            );
            mappingRepository.save(mapping);

            logSync(entityType, "INSERT", request.oldId(), category.id().value(), "SUCCESS", null, startTime);

            log.info("Synced category {} -> {}", request.oldId(), category.id().value());
            return new SyncResultResponse(true, category.id().value(), "Category synced successfully", request.oldId());

        } catch (Exception e) {
            log.error("Failed to sync category {}", request.oldId(), e);
            logSync("CATEGORY_" + request.type(), "INSERT", request.oldId(), null, "ERROR", e.getMessage(), startTime);
            return new SyncResultResponse(false, null, "Error: " + e.getMessage(), request.oldId());
        }
    }

    private UUID resolveAccountId(Integer oldAccountId) {
        if (oldAccountId == null) {
            return null;
        }
        return mappingRepository.findByEntityTypeAndOldId("ACCOUNT", oldAccountId)
                .map(MigrationEntityMapping::getNewId)
                .orElse(null);
    }

    private UUID resolveCategoryId(Integer oldCategoryId, String type) {
        if (oldCategoryId == null) {
            return null;
        }
        return mappingRepository.findByEntityTypeAndOldId("CATEGORY_" + type, oldCategoryId)
                .map(MigrationEntityMapping::getNewId)
                .orElse(null);
    }

    private UserId resolveUserId(Integer oldUserId) {
        if (oldUserId == null) {
            return new UserId("migration-script");
        }
        return mappingRepository.findByEntityTypeAndOldId("USER", oldUserId)
                .map(m -> new UserId(m.getOldName()))
                .orElse(new UserId("migration-script"));
    }

    private GroupId resolveGroupId(Integer oldGroupId) {
        if (oldGroupId == null) {
            return null;
        }
        return mappingRepository.findByEntityTypeAndOldId("GROUP", oldGroupId)
                .map(m -> new GroupId(m.getNewId()))
                .orElse(null);
    }

    private Currency mapCurrency(Integer currencyId) {
        if (currencyId == null) {
            return Currency.PLN;
        }
        return switch (currencyId) {
            case 1 -> Currency.PLN;
            case 2 -> Currency.EUR;
            case 3 -> Currency.USD;
            default -> Currency.PLN;
        };
    }

    private void logSync(String entityType, String operation, Integer oldId, UUID newId, String status, String errorMessage, long startTime) {
        int processingTime = (int) (System.currentTimeMillis() - startTime);
        MigrationSyncLog log = new MigrationSyncLog(entityType, operation, oldId, newId, status);
        log.setErrorMessage(errorMessage);
        log.setProcessingTime(processingTime);
        syncLogRepository.save(log);
    }
}
