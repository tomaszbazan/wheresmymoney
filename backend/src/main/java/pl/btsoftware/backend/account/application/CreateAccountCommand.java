package pl.btsoftware.backend.account.application;

import static java.util.Objects.isNull;
import static pl.btsoftware.backend.shared.AccountId.generate;

import org.springframework.lang.Nullable;
import pl.btsoftware.backend.account.domain.Account;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.infrastructure.api.UserView;

public record CreateAccountCommand(String name, @Nullable Currency currency, UserId userId) {
    public Account toDomain(User user) {
        return new Account(generate(), name, isNull(currency) ? Currency.DEFAULT : currency, UserView.from(user));
    }
}
