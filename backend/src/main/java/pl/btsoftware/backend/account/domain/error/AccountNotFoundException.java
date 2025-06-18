package pl.btsoftware.backend.account.domain.error;

import java.util.UUID;

public class AccountNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    public AccountNotFoundException(UUID accountId) {
        super(ERROR_CODE, "Account not found with id: " + accountId);
    }
}
