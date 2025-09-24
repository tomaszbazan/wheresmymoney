package pl.btsoftware.backend.account.domain.error;

public class CannotDeleteAccountWithTransactionsException extends BusinessException {
    private static final String ERROR_CODE = "CANNOT_DELETE_ACCOUNT_WITH_TRANSACTIONS";
    private static final String MESSAGE = "Cannot delete account with transaction history";

    public CannotDeleteAccountWithTransactionsException() {
        super(ERROR_CODE, MESSAGE);
    }
}
