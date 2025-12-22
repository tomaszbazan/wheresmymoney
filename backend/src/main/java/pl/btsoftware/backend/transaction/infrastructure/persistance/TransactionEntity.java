package pl.btsoftware.backend.transaction.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransactionEntity {
    @Id
    private UUID id;
    @Column(name = "account_id")
    private UUID accountId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    private String description;
    @Column(name = "category_id")
    private UUID categoryId;
    @Column(name = "transaction_date")
    private LocalDate transactionDate;
    @Column(name = "transaction_hash", nullable = false, length = 64)
    private String transactionHash;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_by_group")
    private UUID createdByGroup;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_by_group")
    private UUID updatedByGroup;
    @Column(name = "is_deleted")
    private boolean isDeleted;
    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public static TransactionEntity fromDomain(Transaction transaction) {
        return new TransactionEntity(
                transaction.id().value(),
                transaction.accountId().value(),
                transaction.amount().value(),
                transaction.amount().currency(),
                transaction.type(),
                transaction.description(),
                transaction.categoryId().value(),
                transaction.transactionDate(),
                transaction.transactionHash().value(),
                transaction.createdAt(),
                transaction.createdBy().value(),
                transaction.ownedBy().value(),
                transaction.lastUpdatedAt(),
                transaction.lastUpdatedBy().value(),
                transaction.ownedBy().value(),
                transaction.tombstone().isDeleted(),
                transaction.tombstone().deletedAt()
        );
    }

    public Transaction toDomain() {
        var createdAuditInfo = AuditInfo.create(createdBy, createdByGroup, createdAt);
        var updatedAuditInfo = AuditInfo.create(updatedBy, updatedByGroup, updatedAt);
        return new Transaction(
                TransactionId.of(id),
                AccountId.from(accountId),
                Money.of(amount, currency),
                type,
                description,
                CategoryId.of(categoryId),
                transactionDate,
                new TransactionHash(transactionHash),
                createdAuditInfo,
                updatedAuditInfo,
                new Tombstone(isDeleted, deletedAt)
        );
    }
}
