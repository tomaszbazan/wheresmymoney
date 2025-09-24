package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionAccessDeniedException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_ACCESS_DENIED";
    private static final String MESSAGE = "User cannot access transaction from different group";

    public TransactionAccessDeniedException() {
        super(ERROR_CODE, MESSAGE);
    }
}
