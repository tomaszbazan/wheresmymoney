package pl.btsoftware.backend.transfer.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransferDescriptionTooLongException extends BusinessException {
    private static final String ERROR_CODE = "TRANSFER_DESCRIPTION_TOO_LONG";
    private static final String MESSAGE =
            "Transfer description cannot be longer than 100 characters";

    public TransferDescriptionTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
