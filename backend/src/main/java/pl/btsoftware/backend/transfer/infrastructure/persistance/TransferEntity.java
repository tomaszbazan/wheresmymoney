package pl.btsoftware.backend.transfer.infrastructure.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.*;
import pl.btsoftware.backend.transfer.domain.Transfer;

@Entity
@Table(name = "transfer")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransferEntity {
    @Id
    private UUID id;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "target_account_id")
    private UUID targetAccountId;

    @Column(name = "source_amount")
    private BigDecimal sourceAmount;

    @Column(name = "source_currency")
    @Enumerated(EnumType.STRING)
    private Currency sourceCurrency;

    @Column(name = "target_amount")
    private BigDecimal targetAmount;

    @Column(name = "target_currency")
    @Enumerated(EnumType.STRING)
    private Currency targetCurrency;

    @Column(name = "exchange_rate")
    private BigDecimal exchangeRate;

    private String description;

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

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    @Version
    private Long version;

    public static TransferEntity fromDomain(Transfer transfer) {
        return new TransferEntity(
                transfer.id().value(),
                transfer.sourceAccountId().value(),
                transfer.targetAccountId().value(),
                transfer.sourceAmount().value(),
                transfer.sourceAmount().currency(),
                transfer.targetAmount().value(),
                transfer.targetAmount().currency(),
                transfer.exchangeRate().rate(),
                transfer.description(),
                transfer.createdInfo().when(),
                transfer.createdInfo().who().value(),
                transfer.createdInfo().fromGroup().value(),
                transfer.updatedInfo().when(),
                transfer.updatedInfo().who().value(),
                transfer.tombstone().isDeleted(),
                transfer.tombstone().deletedAt(),
                null);
    }

    public Transfer toDomain() {
        var createdAuditInfo = AuditInfo.create(createdBy, createdByGroup, createdAt);
        var updatedAuditInfo = AuditInfo.create(updatedBy, createdByGroup, updatedAt);
        return new Transfer(
                TransferId.from(id),
                AccountId.from(sourceAccountId),
                AccountId.from(targetAccountId),
                Money.of(sourceAmount, sourceCurrency),
                Money.of(targetAmount, targetCurrency),
                new ExchangeRate(exchangeRate, sourceCurrency, targetCurrency),
                description,
                createdAuditInfo,
                updatedAuditInfo,
                new Tombstone(isDeleted, deletedAt));
    }
}
