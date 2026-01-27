package pl.btsoftware.backend.category.infrastructure.api;

import java.util.List;
import pl.btsoftware.backend.category.domain.Category;

public record CategoriesView(List<CategoryView> categories) {
    public CategoriesView(List<CategoryView> categories) {
        this.categories = categories != null ? List.copyOf(categories) : List.of();
    }

    public static CategoriesView from(List<Category> categories) {
        var categoryViews = categories.stream().map(CategoryView::from).toList();
        return new CategoriesView(categoryViews);
    }
}
