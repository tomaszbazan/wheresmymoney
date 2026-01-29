package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class BillItemsMustHaveSameCurrencyException extends BusinessException {
    private static final String ERROR_CODE = "BILL_ITEMS_MUST_HAVE_SAME_CURRENCY";
    private static final String MESSAGE = "All bill items must have the same currency";

    public BillItemsMustHaveSameCurrencyException() {
        super(ERROR_CODE, MESSAGE);
    }
}
