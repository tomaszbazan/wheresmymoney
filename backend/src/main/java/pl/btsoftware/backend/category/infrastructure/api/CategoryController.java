package pl.btsoftware.backend.category.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@AllArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryModuleFacade categoryModuleFacade;

    @PostMapping
    public CategoryView createCategory(@RequestBody CreateCategoryRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to create category: {} by user: {}", request.name(), userId);
        var category = categoryModuleFacade.createCategory(request.toCommand(userId));
        return CategoryView.from(category);
    }

    @GetMapping("/{id}")
    public CategoryView getCategory(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get category with id: {} by user: {}", id, userId);
        var category = categoryModuleFacade.getCategoryById(new CategoryId(id), userId);
        return CategoryView.from(category);
    }

    @GetMapping
    public CategoriesView getAllCategories(@RequestParam CategoryType type, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to get categories by user: {} with type filter: {}", userId, type);

        var categories = categoryModuleFacade.getCategoriesByType(type, userId);
        return CategoriesView.from(categories);
    }

    @PutMapping("/{id}")
    public CategoryView updateCategory(@PathVariable UUID id, @RequestBody UpdateCategoryRequest request, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to update category with id: {} by user: {}", id, userId);
        var category = categoryModuleFacade.updateCategory(request.toCommand(id), userId);
        return CategoryView.from(category);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        var userId = new UserId(jwt.getSubject());
        log.info("Received request to delete category with id: {} by user: {}", id, userId);
        categoryModuleFacade.deleteCategory(id, userId);
    }
}