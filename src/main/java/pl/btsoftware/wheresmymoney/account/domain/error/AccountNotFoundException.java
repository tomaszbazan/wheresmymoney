package pl.btsoftware.wheresmymoney.account.domain.error;

import java.util.UUID;

/**
 * Exception thrown when an account is not found.
 */
public class AccountNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NOT_FOUND";

    /**
     * Creates a new AccountNotFoundException with the specified account ID.
     *
     * @param accountId the ID of the account that was not found
     */
    public AccountNotFoundException(UUID accountId) {
        super(ERROR_CODE, "Account not found with id: " + accountId);
    }
}
