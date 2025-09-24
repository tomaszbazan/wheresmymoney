package pl.btsoftware.backend.account.domain.error;

public class AccountNameEmptyException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_EMPTY";
    private static final String MESSAGE = "Account name cannot be empty";

    public AccountNameEmptyException() {
        super(ERROR_CODE, MESSAGE);
    }
}
