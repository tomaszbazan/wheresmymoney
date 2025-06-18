package pl.btsoftware.backend.account.domain.error;

public class AccountNameTooLongException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_TOO_LONG";
    private static final String MESSAGE = "Account name cannot be longer than 100 characters";

    public AccountNameTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}