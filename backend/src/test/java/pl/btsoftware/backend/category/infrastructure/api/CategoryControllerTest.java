package pl.btsoftware.backend.category.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.CategoryModuleFacade;
import pl.btsoftware.backend.category.application.CreateCategoryCommand;
import pl.btsoftware.backend.category.application.UpdateCategoryCommand;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.domain.error.CategoryNotFoundException;
import pl.btsoftware.backend.config.WebConfig;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Tombstone;
import pl.btsoftware.backend.users.domain.UserId;

import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.UUID.randomUUID;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.btsoftware.backend.shared.CategoryType.EXPENSE;
import static pl.btsoftware.backend.shared.CategoryType.INCOME;
import static pl.btsoftware.backend.shared.JwtTokenFixture.createTokenFor;

@WebMvcTest(controllers = CategoryController.class)
@Import(WebConfig.class)
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryModuleFacade categoryModuleFacade;

    @Test
    void shouldCreateCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var categoryId = CategoryId.generate();
        var createdCategory = createCategory(categoryId, "Food", EXPENSE, "#FF5722");

        when(categoryModuleFacade.createCategory(any(CreateCategoryCommand.class))).thenReturn(createdCategory);

        var createCategoryRequest = """
                {
                    "name": "Food",
                    "type": "EXPENSE",
                    "color": "#FF5722"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/categories")
                        .contentType(APPLICATION_JSON)
                        .content(createCategoryRequest)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(categoryId.value().toString()))
                .andExpect(jsonPath("$.name").value("Food"))
                .andExpect(jsonPath("$.type").value("EXPENSE"))
                .andExpect(jsonPath("$.color").value("#FF5722"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }


    @Test
    void shouldGetCategoryById() throws Exception {
        // given
        var userId = UserId.generate();
        var categoryId = CategoryId.generate();
        var category = createCategory(categoryId, "Salary", INCOME, "#4CAF50");

        when(categoryModuleFacade.getCategoryById(categoryId, userId)).thenReturn(category);

        // when & then
        mockMvc.perform(get("/api/categories/" + categoryId.value())
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(categoryId.value().toString()))
                .andExpect(jsonPath("$.name").value("Salary"))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.color").value("#4CAF50"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldReturnNotFoundWhenGettingNonExistentCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var nonExistentId = CategoryId.generate();

        when(categoryModuleFacade.getCategoryById(nonExistentId, userId))
                .thenThrow(new CategoryNotFoundException(nonExistentId));

        // when & then
        mockMvc.perform(get("/api/categories/" + nonExistentId.value())
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Category not found with id: " + nonExistentId.value())));
    }

    @ParameterizedTest
    @EnumSource(CategoryType.class)
    void shouldGetCategoriesByType(CategoryType type) throws Exception {
        // given
        var userId = UserId.generate();
        var categoryId1 = CategoryId.generate();
        var categoryId2 = CategoryId.generate();
        var category1 = createCategory(categoryId1, "Category 1", type, "#FF5722");
        var category2 = createCategory(categoryId2, "Category 2", type, "#2196F3");

        when(categoryModuleFacade.getCategoriesByType(type, userId))
                .thenReturn(List.of(category1, category2));

        // when & then
        mockMvc.perform(get("/api/categories")
                        .param("type", type.name())
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.categories", hasSize(2)))
                .andExpect(jsonPath("$.categories[0].id").value(categoryId1.value().toString()))
                .andExpect(jsonPath("$.categories[0].type").value(type.name()))
                .andExpect(jsonPath("$.categories[1].id").value(categoryId2.value().toString()))
                .andExpect(jsonPath("$.categories[1].type").value(type.name()));
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExistForType() throws Exception {
        // given
        var userId = UserId.generate();
        when(categoryModuleFacade.getCategoriesByType(EXPENSE, userId)).thenReturn(emptyList());

        // when & then
        mockMvc.perform(get("/api/categories")
                        .param("type", "EXPENSE")
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.categories", hasSize(0)));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var categoryId = CategoryId.generate();
        var updatedCategory = createCategory(categoryId, "Updated Food", EXPENSE, "#FF9800");

        when(categoryModuleFacade.updateCategory(any(UpdateCategoryCommand.class), eq(userId)))
                .thenReturn(updatedCategory);

        var updateRequest = new UpdateCategoryRequest("Updated Food", Color.of("#FF9800"), null);

        // when & then
        mockMvc.perform(put("/api/categories/" + categoryId.value())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(categoryId.value().toString()))
                .andExpect(jsonPath("$.name").value("Updated Food"))
                .andExpect(jsonPath("$.color").value("#FF9800"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var nonExistentId = CategoryId.generate();

        when(categoryModuleFacade.updateCategory(any(UpdateCategoryCommand.class), eq(userId)))
                .thenThrow(new CategoryNotFoundException(nonExistentId));

        var updateRequest = new UpdateCategoryRequest("Updated Name", Color.of("#FF5722"), null);

        // when & then
        mockMvc.perform(put("/api/categories/" + nonExistentId.value())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest))
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Category not found with id: " + nonExistentId.value())));
    }


    @Test
    void shouldDeleteCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var categoryId = randomUUID();

        // when & then
        mockMvc.perform(delete("/api/categories/" + categoryId)
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentCategory() throws Exception {
        // given
        var userId = UserId.generate();
        var nonExistentId = randomUUID();

        doThrow(new CategoryNotFoundException(CategoryId.of(nonExistentId)))
                .when(categoryModuleFacade).deleteCategory(nonExistentId, userId);

        // when & then
        mockMvc.perform(delete("/api/categories/" + nonExistentId)
                        .contentType(APPLICATION_JSON)
                        .with(createTokenFor(userId.value())))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Category not found with id: " + nonExistentId)));
    }

    private Category createCategory(CategoryId categoryId, String name, CategoryType type, String color) {
        return new Category(
                categoryId,
                name,
                type,
                Color.of(color),
                null,
                Instancio.create(AuditInfo.class),
                Instancio.create(AuditInfo.class),
                Tombstone.active()
        );
    }
}
