package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class BillItemDescriptionInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "BILL_ITEM_DESCRIPTION_INVALID_CHARACTERS";
    private static final String MESSAGE = "Bill item description contains invalid characters";

    public BillItemDescriptionInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
