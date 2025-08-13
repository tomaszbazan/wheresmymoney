package pl.btsoftware.backend.transaction.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;

import java.math.BigDecimal;
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
    private String category;
    @Column(name = "created_at")
    private OffsetDateTime createdAt;
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
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
                transaction.category(),
                transaction.createdAt(),
                transaction.updatedAt(),
                transaction.tombstone().isDeleted(),
                transaction.tombstone().deletedAt()
        );
    }

    public Transaction toDomain() {
        return new Transaction(
                TransactionId.of(id),
                AccountId.from(accountId),
                Money.of(amount, currency),
                type,
                description,
                category,
                createdAt,
                updatedAt,
                new Tombstone(isDeleted, deletedAt)
        );
    }
}