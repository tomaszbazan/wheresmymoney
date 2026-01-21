package pl.btsoftware.backend.account.domain.error;

public class AccountNameInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_NAME_INVALID_CHARACTERS";
    private static final String MESSAGE = "Account name contains invalid characters";

    public AccountNameInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
