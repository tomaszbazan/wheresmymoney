package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CategoryNameTooLongException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_NAME_TOO_LONG";

    public CategoryNameTooLongException() {
        super(ERROR_CODE, "Category name cannot be longer than 100 characters");
    }
}
