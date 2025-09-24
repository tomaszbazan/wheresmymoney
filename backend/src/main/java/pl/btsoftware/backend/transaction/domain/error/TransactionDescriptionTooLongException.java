package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionDescriptionTooLongException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_DESCRIPTION_TOO_LONG";
    private static final String MESSAGE = "Description cannot exceed 200 characters";

    public TransactionDescriptionTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
