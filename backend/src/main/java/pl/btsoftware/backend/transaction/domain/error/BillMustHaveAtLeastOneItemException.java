package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class BillMustHaveAtLeastOneItemException extends BusinessException {
    private static final String ERROR_CODE = "BILL_MUST_HAVE_AT_LEAST_ONE_ITEM";
    private static final String MESSAGE = "Bill must have at least one item";

    public BillMustHaveAtLeastOneItemException() {
        super(ERROR_CODE, MESSAGE);
    }
}
