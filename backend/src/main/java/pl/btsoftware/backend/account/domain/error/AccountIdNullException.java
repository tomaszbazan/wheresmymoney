package pl.btsoftware.backend.account.domain.error;

public class AccountIdNullException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_ID_NULL";
    private static final String MESSAGE = "Account id cannot be null";

    public AccountIdNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}