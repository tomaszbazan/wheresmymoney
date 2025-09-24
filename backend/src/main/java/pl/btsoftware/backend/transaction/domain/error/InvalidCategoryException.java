package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.CategoryId;

public class InvalidCategoryException extends BusinessException {
    private static final String ERROR_CODE = "INVALID_CATEGORY";

    public InvalidCategoryException(CategoryId categoryId) {
        super(ERROR_CODE, "Category not found or invalid: " + categoryId.value());
    }
}
