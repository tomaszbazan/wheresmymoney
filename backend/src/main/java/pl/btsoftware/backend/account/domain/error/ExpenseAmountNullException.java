package pl.btsoftware.backend.account.domain.error;

/**
 * Exception thrown when an expense amount is null.
 */
public class ExpenseAmountNullException extends BusinessException {
    private static final String ERROR_CODE = "EXPENSE_AMOUNT_NULL";
    private static final String MESSAGE = "Amount cannot be null";

    /**
     * Creates a new ExpenseAmountNullException.
     */
    public ExpenseAmountNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}