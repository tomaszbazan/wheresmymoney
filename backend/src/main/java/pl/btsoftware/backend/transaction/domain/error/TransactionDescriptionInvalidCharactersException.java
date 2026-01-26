package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionDescriptionInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_DESCRIPTION_INVALID_CHARACTERS";
    private static final String MESSAGE = "Transaction description contains invalid characters";

    public TransactionDescriptionInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
