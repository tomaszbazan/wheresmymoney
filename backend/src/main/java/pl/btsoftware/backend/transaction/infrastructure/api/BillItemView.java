package pl.btsoftware.backend.transaction.infrastructure.api;

import java.util.UUID;
import java.util.function.Function;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.transaction.domain.BillItem;

public record BillItemView(UUID id, CategoryView category, Money money, String description) {
    public static BillItemView from(BillItem item, Function<CategoryId, Category> categoryMapper) {
        var category = categoryMapper.apply(item.categoryId());
        return new BillItemView(item.id().value(), CategoryView.from(category), item.amount(), item.description());
    }

    record CategoryView(UUID id, String name) {
        public static CategoryView from(Category category) {
            return new CategoryView(category.id().value(), category.name());
        }
    }
}
