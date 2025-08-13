package pl.btsoftware.backend.account.application;

import pl.btsoftware.backend.account.domain.error.AccountNameEmptyException;
import pl.btsoftware.backend.shared.AccountId;

public record UpdateAccountCommand(AccountId accountId, String name) {
    public UpdateAccountCommand {
        if (accountId == null) {
            throw new IllegalArgumentException("Account id cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new AccountNameEmptyException();
        }
    }
}
