package pl.btsoftware.backend.account.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.shared.TransactionId;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "account")
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AccountEntity {
    @Id
    private UUID id;
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
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private AccountData data;

    public static AccountEntity fromDomain(Account account) {
        var accountData = AccountData.from(account);
        return new AccountEntity(
                account.id().value(),
                account.name(),
                account.balance().value(),
                account.balance().currency(),
                account.createdAt(),
                account.createdBy().value(),
                account.ownedBy().value(),
                account.lastUpdatedAt(),
                account.lastUpdatedBy().value(),
                accountData
        );
    }

    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
        List<TransactionId> domainTransactionIds = (data != null)
                ? data.toTransactionIds()
                : new ArrayList<>();
        var createdAuditInfo = AuditInfo.create(createdBy, ownedByGroup, createdAt);
        var updatedAuditInfo = AuditInfo.create(updatedBy, ownedByGroup, updatedAt);
        return new Account(accountId, name, Money.of(balance, currency), domainTransactionIds, createdAuditInfo, updatedAuditInfo, Tombstone.active());
    }
}
