package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.TransactionId;

public class TransactionNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_NOT_FOUND";

    public TransactionNotFoundException(TransactionId transactionId) {
        super(ERROR_CODE, "Transaction not found with id: " + transactionId.value());
    }
}
