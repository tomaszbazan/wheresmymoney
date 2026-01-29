package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class BillItemDescriptionTooLongException extends BusinessException {
    private static final String ERROR_CODE = "BILL_ITEM_DESCRIPTION_TOO_LONG";
    private static final String MESSAGE = "Bill item description cannot exceed 100 characters";

    public BillItemDescriptionTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
