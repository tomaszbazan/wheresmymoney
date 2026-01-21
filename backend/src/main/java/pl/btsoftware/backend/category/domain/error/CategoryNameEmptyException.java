package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CategoryNameEmptyException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_NAME_EMPTY";
    private static final String MESSAGE = "Category name cannot be empty";

    public CategoryNameEmptyException() {
        super(ERROR_CODE, MESSAGE);
    }
}
