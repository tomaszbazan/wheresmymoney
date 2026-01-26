package pl.btsoftware.backend.transfer.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransferToSameAccountException extends BusinessException {
    private static final String ERROR_CODE = "TRANSFER_TO_SAME_ACCOUNT";
    private static final String MESSAGE = "Cannot transfer money to the same account";

    public TransferToSameAccountException() {
        super(ERROR_CODE, MESSAGE);
    }
}
