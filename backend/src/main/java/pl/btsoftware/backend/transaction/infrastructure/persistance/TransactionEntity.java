package pl.btsoftware.backend.transaction.infrastructure.persistance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transaction.domain.Transaction;
import pl.btsoftware.backend.transaction.domain.TransactionHash;

@Entity
@Table(name = "transaction")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransactionEntity {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Id
    private UUID id;

    @Column(name = "account_id")
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "bill", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String bill;

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
        try {
            var billJson = BillJson.fromDomain(transaction.bill());
            return new TransactionEntity(
                    transaction.id().value(),
                    transaction.accountId().value(),
                    transaction.type(),
                    OBJECT_MAPPER.writeValueAsString(billJson),
                    transaction.transactionDate(),
                    transaction.transactionHash().value(),
                    transaction.createdAt(),
                    transaction.createdBy().value(),
                    transaction.ownedBy().value(),
                    transaction.lastUpdatedAt(),
                    transaction.lastUpdatedBy().value(),
                    transaction.ownedBy().value(),
                    transaction.tombstone().isDeleted(),
                    transaction.tombstone().deletedAt());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize bill to JSON", e);
        }
    }

    public Transaction toDomain() {
        try {
            var billJson = OBJECT_MAPPER.readValue(this.bill, BillJson.class);
            var billDomain = billJson.toDomain();
            var createdAuditInfo = AuditInfo.create(createdBy, createdByGroup, createdAt);
            var updatedAuditInfo = AuditInfo.create(updatedBy, updatedByGroup, updatedAt);
            return new Transaction(
                    TransactionId.of(id),
                    AccountId.from(accountId),
                    type,
                    billDomain,
                    transactionDate,
                    new TransactionHash(transactionHash),
                    createdAuditInfo,
                    updatedAuditInfo,
                    new Tombstone(isDeleted, deletedAt));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize bill from JSON", e);
        }
    }
}
