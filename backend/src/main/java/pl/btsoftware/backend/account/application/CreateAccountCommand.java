package pl.btsoftware.backend.account.application;

import org.springframework.lang.Nullable;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.Currency;

import static pl.btsoftware.backend.shared.AccountId.generate;

public record CreateAccountCommand(String name, @Nullable Currency currency) {
    public Account toDomain() {
        return new Account(generate(), name, currency);
    }
}
