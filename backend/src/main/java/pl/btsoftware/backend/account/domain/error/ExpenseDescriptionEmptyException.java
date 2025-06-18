package pl.btsoftware.backend.account.domain.error;

/**
 * Exception thrown when an expense description is null or blank.
 */
public class ExpenseDescriptionEmptyException extends BusinessException {
    private static final String ERROR_CODE = "EXPENSE_DESCRIPTION_EMPTY";
    private static final String MESSAGE = "Description cannot be null or blank";

    /**
     * Creates a new ExpenseDescriptionEmptyException.
     */
    public ExpenseDescriptionEmptyException() {
        super(ERROR_CODE, MESSAGE);
    }
}