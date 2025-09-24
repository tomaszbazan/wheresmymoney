package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class CategoryAccessDeniedException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_ACCESS_DENIED";

    public CategoryAccessDeniedException() {
        super(ERROR_CODE, "Access to category denied");
    }
}
