package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.TransactionId;

public class TransactionAlreadyDeletedException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_ALREADY_DELETED";

    public TransactionAlreadyDeletedException(TransactionId transactionId) {
        super(ERROR_CODE, "Transaction with id " + transactionId.value() + " is already deleted");
    }
}
