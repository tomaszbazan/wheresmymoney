package pl.btsoftware.backend.account.domain.error;

public class AccountAlreadyExistsException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_ID_NULL";
    private static final String MESSAGE = "Account name already exists: ";

    public AccountAlreadyExistsException(String name) {
        super(ERROR_CODE, MESSAGE + name);
    }
}