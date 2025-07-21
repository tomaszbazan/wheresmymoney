package pl.btsoftware.backend.account.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.account.domain.AccountId;
import pl.btsoftware.backend.account.domain.Currency;
import pl.btsoftware.backend.account.domain.Money;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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

    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
            account.id().value(),
            account.name(),
            account.balance().amount(),
            account.balance().currency(),
            account.createdAt(),
            account.updatedAt()
        );
    }

    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
        return new Account(accountId, name, Money.of(balance, currency), createdAt, updatedAt);
    }
}
