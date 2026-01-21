package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionHashInvalidException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_HASH_INVALID";
    private static final String MESSAGE = "Invalid transaction hash";

    public TransactionHashInvalidException() {
        super(ERROR_CODE, MESSAGE);
    }
}
