package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.CategoryId;

public class CategoryNotFoundException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_NOT_FOUND";

    public CategoryNotFoundException(CategoryId categoryId) {
        super(ERROR_CODE, "Category not found with id: " + categoryId.value());
    }
}
