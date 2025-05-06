package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Exception thrown when an account name is empty or blank.
 */
public class AccountNameEmptyException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_EMPTY";
    private static final String MESSAGE = "Account name cannot be empty";

    /**
     * Creates a new AccountNameEmptyException.
     */
    public AccountNameEmptyException() {
        super(ERROR_CODE, MESSAGE);
    }
}