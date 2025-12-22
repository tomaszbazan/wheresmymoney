package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClient;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClientException;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.category.infrastructure.persistance.InMemoryCategoryRepository;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategorySuggestionServiceTest {

    private InMemoryCategoryRepository categoryRepository;
    private GeminiClient geminiClient;
    private CategorizationPromptBuilder promptBuilder;
    private GeminiResponseParser responseParser;
    private CategorySuggestionService service;
    private GroupId testGroupId;
    private AuditInfo testAuditInfo;

    @BeforeEach
    void setUp() {
        categoryRepository = new InMemoryCategoryRepository();
        geminiClient = mock(GeminiClient.class);
        promptBuilder = new CategorizationPromptBuilder();
        responseParser = new GeminiResponseParser();
        service = new CategorySuggestionService(categoryRepository, geminiClient, promptBuilder, responseParser);
        testGroupId = GroupId.generate();
        testAuditInfo = AuditInfo.create(UserId.generate(), testGroupId);
    }

    @Test
    void shouldReturnSuggestionsForValidTransactions() {
        // given
        var foodCategory = createAndStoreCategory("Food", CategoryType.EXPENSE);
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        var geminiResponse = String.format("""
                [
                  {
                    "transactionId": "00000000-0000-0000-0000-000000000000",
                    "categoryId": "%s",
                    "confidence": 0.95
                  }
                ]
                """, foodCategory.id().value());

        when(geminiClient.generateContent(any())).thenReturn(CompletableFuture.completedFuture(geminiResponse));

        // when
        var result = service.suggestCategories(List.of(transaction), testGroupId);

        // then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(foodCategory.id(), result.getFirst().categoryId());
        assertEquals(0.95, result.getFirst().confidence());
    }

    @Test
    void shouldFilterCategoriesByTransactionType() {
        // given
        var expenseCategory = createAndStoreCategory("Food", CategoryType.EXPENSE);
        createAndStoreCategory("Salary", CategoryType.INCOME);
        var expenseTransaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        var geminiResponse = String.format("""
                [
                  {
                    "transactionId": "00000000-0000-0000-0000-000000000000",
                    "categoryId": "%s",
                    "confidence": 0.95
                  }
                ]
                """, expenseCategory.id().value());

        when(geminiClient.generateContent(any())).thenReturn(CompletableFuture.completedFuture(geminiResponse));

        // when
        service.suggestCategories(List.of(expenseTransaction), testGroupId);

        // then
        verify(geminiClient, times(1)).generateContent(any());
    }

    @Test
    void shouldReturnNullSuggestionsWhenGeminiFails() {
        // given
        createAndStoreCategory("Food", CategoryType.EXPENSE);
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        when(geminiClient.generateContent(any())).thenReturn(CompletableFuture.failedFuture(new GeminiClientException("API Error")));

        // when
        var result = service.suggestCategories(List.of(transaction), testGroupId);

        // then
        assertNull(result);
    }

    @Test
    void shouldThrowExceptionWhenTransactionListIsEmpty() {
        // when & then
        assertThrows(IllegalArgumentException.class, () -> service.suggestCategories(List.of(), testGroupId));
    }

    @Test
    void shouldThrowExceptionWhenGroupIdIsNull() {
        // given
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> service.suggestCategories(List.of(transaction), null));
    }

    @Test
    void shouldHandleTransactionsWithMixedTypes() {
        // given
        var expenseCategory = createAndStoreCategory("Food", CategoryType.EXPENSE);
        var incomeCategory = createAndStoreCategory("Salary", CategoryType.INCOME);
        var expenseTransaction = createTransaction("McDonald's", TransactionType.EXPENSE);
        var incomeTransaction = createTransaction("Monthly salary", TransactionType.INCOME);

        var expenseResponse = String.format("""
                [
                  {
                    "transactionId": "00000000-0000-0000-0000-000000000000",
                    "categoryId": "%s",
                    "confidence": 0.95
                  }
                ]
                """, expenseCategory.id().value());

        var incomeResponse = String.format("""
                [
                  {
                    "transactionId": "00000000-0000-0000-0000-000000000001",
                    "categoryId": "%s",
                    "confidence": 0.90
                  }
                ]
                """, incomeCategory.id().value());

        when(geminiClient.generateContent(any())).thenReturn(CompletableFuture.completedFuture(expenseResponse)).thenReturn(CompletableFuture.completedFuture(incomeResponse));

        // when
        var result = service.suggestCategories(List.of(expenseTransaction, incomeTransaction), testGroupId);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoriesExist() {
        // given
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        // when
        var result = service.suggestCategories(List.of(transaction), testGroupId);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(geminiClient, never()).generateContent(any());
    }

    private Category createAndStoreCategory(String name, CategoryType type) {
        var category = Category.create(name, type, Color.of("#FF0000"), testAuditInfo);
        categoryRepository.store(category);
        return category;
    }

    private TransactionProposal createTransaction(String description, TransactionType type) {
        return new TransactionProposal(LocalDate.now(), description, BigDecimal.valueOf(100), Currency.PLN, type, null);
    }
}
