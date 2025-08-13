package pl.btsoftware.backend.account.domain.error;

import pl.btsoftware.backend.shared.AccountId;

public class AccountNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    public AccountNotFoundException(AccountId accountId) {
        super(ERROR_CODE, "Account not found with id: " + accountId.value());
    }
}
