package pl.btsoftware.backend.transfer.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransferDescriptionInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "TRANSFER_DESCRIPTION_INVALID_CHARACTERS";
    private static final String MESSAGE = "Transfer description contains invalid characters";

    public TransferDescriptionInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
