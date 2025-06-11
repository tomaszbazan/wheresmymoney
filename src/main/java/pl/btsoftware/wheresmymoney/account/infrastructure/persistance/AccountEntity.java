package pl.btsoftware.wheresmymoney.account.infrastructure.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.btsoftware.wheresmymoney.account.domain.Account;
import pl.btsoftware.wheresmymoney.account.domain.AccountId;
import pl.btsoftware.wheresmymoney.account.domain.Money;

import java.math.BigDecimal;
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
    private String currency;

    public static AccountEntity fromDomain(Account account) {
        return new AccountEntity(
            account.id().value(),
            account.name(),
                account.balance().amount(),
                account.balance().currency()
        );
    }

    public Account toDomain() {
        AccountId accountId = AccountId.from(id);
        return new Account(accountId, name, Money.of(balance, currency), java.time.OffsetDateTime.now());
    }
}
