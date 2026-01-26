package pl.btsoftware.backend.account.infrastructure.persistance;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.Tombstone;

@Entity
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AccountEntity {
    @Id private UUID id;
    private String name;
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "created_by_group")
    private UUID ownedByGroup;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Version private Long version;

    public AccountEntity(
            UUID id,
            String name,
            BigDecimal balance,
            Currency currency,
            OffsetDateTime createdAt,
            String createdBy,
            UUID ownedByGroup,
            OffsetDateTime updatedAt,
            String updatedBy) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.ownedByGroup = ownedByGroup;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
                account.id().value(),
                account.name(),
                account.balance().value(),
                account.balance().currency(),
                account.createdAt(),
                account.createdBy().value(),
                account.ownedBy().value(),
                account.lastUpdatedAt(),
                account.lastUpdatedBy().value());
    }

    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
        var createdAuditInfo = AuditInfo.create(createdBy, ownedByGroup, createdAt);
        var updatedAuditInfo = AuditInfo.create(updatedBy, ownedByGroup, updatedAt);
        return new Account(
                accountId,
                name,
                Money.of(balance, currency),
                createdAuditInfo,
                updatedAuditInfo,
                Tombstone.active());
    }
}
