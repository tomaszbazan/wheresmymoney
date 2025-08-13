package pl.btsoftware.backend.account.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
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
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data", columnDefinition = "jsonb")
    private AccountData data;

    public static AccountEntity fromDomain(Account account) {
        AccountData accountData = AccountData.from(account.transactionIds());
        return new AccountEntity(
            account.id().value(),
            account.name(),
                account.balance().value(),
            account.balance().currency(),
            account.createdAt(),
                account.updatedAt(),
                accountData
        );
    }

    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
        List<TransactionId> domainTransactionIds = (data != null)
                ? data.toTransactionIds()
                : new ArrayList<>();
        return new Account(accountId, name, Money.of(balance, currency), domainTransactionIds, createdAt, updatedAt);
    }
}
