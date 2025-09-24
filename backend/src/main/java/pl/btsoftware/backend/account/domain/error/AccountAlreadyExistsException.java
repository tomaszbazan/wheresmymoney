package pl.btsoftware.backend.account.domain.error;

public class AccountAlreadyExistsException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_ALREADY_EXISTS";
    private static final String MESSAGE = "Account with provided name and currency already exists";

    public AccountAlreadyExistsException() {
        super(ERROR_CODE, MESSAGE);
    }
}
