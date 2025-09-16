package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CategoryHierarchyTooDeepException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_HIERARCHY_TOO_DEEP";

    public CategoryHierarchyTooDeepException() {
        super(ERROR_CODE, "Category hierarchy cannot exceed 5 levels");
    }
}