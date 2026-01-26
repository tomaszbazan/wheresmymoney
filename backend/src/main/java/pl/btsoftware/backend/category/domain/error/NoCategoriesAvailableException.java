package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.CategoryType;

public class NoCategoriesAvailableException extends BusinessException {
    private static final String ERROR_CODE = "NO_CATEGORIES_AVAILABLE";

    public NoCategoriesAvailableException(CategoryType type) {
        super(
                ERROR_CODE,
                "No categories of type "
                        + type
                        + " are available. Please create categories first.");
    }
}
