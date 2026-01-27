package pl.btsoftware.backend.category.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.CategoryId;

public class CategoryHasTransactionsException extends BusinessException {
    private static final String ERROR_CODE = "CATEGORY_HAS_TRANSACTIONS";

    public CategoryHasTransactionsException(CategoryId categoryId) {
        super(
                ERROR_CODE,
                "Category cannot be deleted because it has associated transactions: "
                        + categoryId.value());
    }
}
