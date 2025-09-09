package pl.btsoftware.backend.category.infrastructure.api;

import pl.btsoftware.backend.category.domain.Category;

import java.util.List;

public record CategoriesView(List<CategoryView> categories) {
    public static CategoriesView from(List<Category> categories) {
        var categoryViews = categories.stream()
                .map(CategoryView::from)
                .toList();
        return new CategoriesView(categoryViews);
    }
}