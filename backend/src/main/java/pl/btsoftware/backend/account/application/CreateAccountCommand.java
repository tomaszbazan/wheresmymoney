package pl.btsoftware.backend.account.application;

import org.springframework.lang.Nullable;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.infrastructure.api.UserView;

import static pl.btsoftware.backend.shared.AccountId.generate;

public record CreateAccountCommand(String name, @Nullable Currency currency, UserId userId) {
    public Account toDomain(UserView user) {
        return new Account(generate(), name, currency == null ? Currency.DEFAULT : currency, user);
    }
}
