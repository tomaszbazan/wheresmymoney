package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Exception thrown when an account ID is null.
 */
public class AccountIdNullException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_ID_NULL";
    private static final String MESSAGE = "Account id cannot be null";

    /**
     * Creates a new AccountIdNullException.
     */
    public AccountIdNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}