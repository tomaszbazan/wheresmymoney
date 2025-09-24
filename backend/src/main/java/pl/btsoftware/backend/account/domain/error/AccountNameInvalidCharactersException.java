package pl.btsoftware.backend.account.domain.error;

/**
 * Exception thrown when an account name contains invalid characters.
 */
public class AccountNameInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_INVALID_CHARACTERS";
    private static final String MESSAGE = "Account name contains invalid characters";

    /**
     * Creates a new AccountNameInvalidCharactersException.
     */
    public AccountNameInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
