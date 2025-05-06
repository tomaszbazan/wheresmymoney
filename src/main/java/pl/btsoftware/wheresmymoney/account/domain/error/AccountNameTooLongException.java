package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Exception thrown when an account name is too long.
 */
public class AccountNameTooLongException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_TOO_LONG";
    private static final String MESSAGE = "Account name cannot be longer than 255 characters";

    /**
     * Creates a new AccountNameTooLongException.
     */
    public AccountNameTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}