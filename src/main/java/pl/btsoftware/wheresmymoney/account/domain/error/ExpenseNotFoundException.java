package pl.btsoftware.wheresmymoney.account.domain.error;

import java.util.UUID;

/**
 * Exception thrown when an expense is not found.
 */
public class ExpenseNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "EXPENSE_NOT_FOUND";

    /**
     * Creates a new ExpenseNotFoundException with the specified expense ID.
     *
     * @param expenseId the ID of the expense that was not found
     */
    public ExpenseNotFoundException(UUID expenseId) {
        super(ERROR_CODE, "Expense not found with id: " + expenseId);
    }
}