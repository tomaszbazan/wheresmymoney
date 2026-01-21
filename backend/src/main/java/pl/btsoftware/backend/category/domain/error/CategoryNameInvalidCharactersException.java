package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CategoryNameInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_NAME_INVALID_CHARACTERS";
    private static final String MESSAGE = "Category name contains invalid characters";

    public CategoryNameInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
