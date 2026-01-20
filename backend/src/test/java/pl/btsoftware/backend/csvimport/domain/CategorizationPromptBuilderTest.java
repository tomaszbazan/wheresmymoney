package pl.btsoftware.backend.csvimport.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class CategorizationPromptBuilderTest {

    private final CategorizationPromptBuilder builder = new CategorizationPromptBuilder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRejectEmptyTransactionList() {
        var category = createCategory("Food", CategoryType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(), List.of(category)));
    }

    @Test
    void shouldRejectEmptyCategoryList() {
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);
        assertThrows(IllegalArgumentException.class, () -> builder.build(List.of(transaction), List.of()));
    }

    @Test
    void shouldBuildValidPromptForSingleTransactionAndFlatCategories() throws Exception {
        var transaction = createTransaction("McDonald's Downtown", TransactionType.EXPENSE);
        var category = createCategory("Fast Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        assertNotNull(json.get("systemInstructions"));
        assertNotNull(json.get("transactions"));
        assertNotNull(json.get("categories"));
        assertNotNull(json.get("expectedResponseFormat"));

        var transactions = json.get("transactions");
        assertEquals(1, transactions.size());
        assertThat(transactions.get(0).get("transactionId").asText()).isEqualTo(transaction.transactionId().value().toString());
        assertEquals("McDonald's Downtown", transactions.get(0).get("description").asText());
        assertEquals("EXPENSE", transactions.get(0).get("type").asText());

        var categories = json.get("categories");
        assertEquals(1, categories.size());
        assertThat(categories.get(0).get("categoryId").asText()).isEqualTo(category.id().value().toString());
        assertEquals("Fast Food", categories.get(0).get("name").asText());
        assertEquals("EXPENSE", categories.get(0).get("type").asText());
    }

    @Test
    void shouldAssignCorrectIndicesToTransactions() throws Exception {
        var transaction1 = createTransaction("McDonald's", TransactionType.EXPENSE);
        var transaction2 = createTransaction("Salary", TransactionType.INCOME);
        var transaction3 = createTransaction("Starbucks", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction1, transaction2, transaction3), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var transactions = json.get("transactions");
        assertThat(transactions.size()).isEqualTo(3);
        assertThat(transactions.get(0).get("transactionId").asText()).isEqualTo(transaction1.transactionId().value().toString());
        assertThat(transactions.get(1).get("transactionId").asText()).isEqualTo(transaction2.transactionId().value().toString());
        assertThat(transactions.get(2).get("transactionId").asText()).isEqualTo(transaction3.transactionId().value().toString());
    }

    @Test
    void shouldEscapeSpecialCharactersInDescriptions() throws Exception {
        var transaction = createTransaction("\"McDonald's\" - Special\nOffer", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var transactions = json.get("transactions");
        assertEquals("\"McDonald's\" - Special\nOffer", transactions.get(0).get("description").asText());
    }

    @Test
    void shouldIncludeSystemInstructionsInPrompt() throws Exception {
        var transaction = createTransaction("Test", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var instructions = json.get("systemInstructions").asText();
        assertTrue(instructions.contains("financial transaction categorization"));
        assertTrue(instructions.contains("confidence"));
    }

    @Test
    void shouldIncludeExpectedResponseFormatInPrompt() throws Exception {
        var transaction = createTransaction("Test", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var format = json.get("expectedResponseFormat").asText();
        assertTrue(format.contains("transactionId"));
        assertTrue(format.contains("categoryId"));
        assertTrue(format.contains("confidence"));
    }

    private TransactionProposal createTransaction(String description, TransactionType type) {
        return new TransactionProposal(
                new TransactionProposalId(UUID.randomUUID()),
                LocalDate.now(),
                description,
                BigDecimal.valueOf(100),
                Currency.PLN,
                type,
                null
        );
    }

    @Test
    void shouldBuildHierarchicalCategoryStructureCorrectly() throws Exception {
        var auditInfo = AuditInfo.create(UserId.generate(), GroupId.generate());
        var food = Category.create("Food", CategoryType.EXPENSE, Color.of("#FF0000"), auditInfo);
        var restaurants = Category.create("Restaurants", CategoryType.EXPENSE, Color.of("#FF0000"), food.id(), auditInfo);
        var fastFood = Category.create("Fast Food", CategoryType.EXPENSE, Color.of("#FF0000"), restaurants.id(), auditInfo);
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(food, restaurants, fastFood));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var categories = json.get("categories");
        assertEquals(1, categories.size());

        var foodNode = categories.get(0);
        assertEquals(food.id().value().toString(), foodNode.get("categoryId").asText());
        assertEquals("Food", foodNode.get("name").asText());
        assertEquals(1, foodNode.get("children").size());

        var restaurantsNode = foodNode.get("children").get(0);
        assertEquals(restaurants.id().value().toString(), restaurantsNode.get("categoryId").asText());
        assertEquals("Restaurants", restaurantsNode.get("name").asText());
        assertEquals(1, restaurantsNode.get("children").size());

        var fastFoodNode = restaurantsNode.get("children").get(0);
        assertEquals(fastFood.id().value().toString(), fastFoodNode.get("categoryId").asText());
        assertEquals("Fast Food", fastFoodNode.get("name").asText());
        assertEquals(0, fastFoodNode.get("children").size());
    }

    @Test
    void shouldHandleNullCategoryParentIdForRootCategories() throws Exception {
        var category1 = createCategory("Food", CategoryType.EXPENSE);
        var category2 = createCategory("Transport", CategoryType.EXPENSE);
        var transaction = createTransaction("Test", TransactionType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category1, category2));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var categories = json.get("categories");
        assertEquals(2, categories.size());
    }

    private Category createCategory(String name, CategoryType type) {
        var auditInfo = AuditInfo.create(UserId.generate(), GroupId.generate());
        return Category.create(name, type, Color.of("#FF0000"), auditInfo);
    }
}
