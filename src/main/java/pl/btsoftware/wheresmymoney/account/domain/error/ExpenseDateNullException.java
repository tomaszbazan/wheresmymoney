package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Exception thrown when an expense createdAt is null.
 */
public class ExpenseDateNullException extends BusinessException {
    private static final String ERROR_CODE = "EXPENSE_DATE_NULL";
    private static final String MESSAGE = "Date cannot be null";

    /**
     * Creates a new ExpenseDateNullException.
     */
    public ExpenseDateNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}