package pl.btsoftware.wheresmymoney.account.domain.error;

/**
 * Exception thrown when an expense ID is null.
 */
public class ExpenseIdNullException extends BusinessException {
    private static final String ERROR_CODE = "EXPENSE_ID_NULL";
    private static final String MESSAGE = "Expense id cannot be null";

    /**
     * Creates a new ExpenseIdNullException.
     */
    public ExpenseIdNullException() {
        super(ERROR_CODE, MESSAGE);
    }
}