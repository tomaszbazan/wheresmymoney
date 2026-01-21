package pl.btsoftware.backend.account.domain.error;

public class AccountHasTransactionsException extends BusinessException {
    private static final String ERROR_CODE = "ACCOUNT_HAS_TRANSACTIONS";
    private static final String MESSAGE = "Cannot delete account with existing transactions";

    public AccountHasTransactionsException() {
        super(ERROR_CODE, MESSAGE);
    }
}
