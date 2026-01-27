package pl.btsoftware.backend.csvimport.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.account.domain.AuditInfo;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.Color;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

class CategorizationPromptBuilderTest {

    private final CategorizationPromptBuilder builder = new CategorizationPromptBuilder();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldRejectEmptyTransactionList() {
        var category = createCategory("Food", CategoryType.EXPENSE);
        assertThatThrownBy(() -> builder.build(List.of(), List.of(category)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldRejectEmptyCategoryList() {
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);
        assertThatThrownBy(() -> builder.build(List.of(transaction), List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBuildValidPromptForSingleTransactionAndFlatCategories() throws Exception {
        var transaction = createTransaction("McDonald's Downtown", TransactionType.EXPENSE);
        var category = createCategory("Fast Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        assertThat(json.get("systemInstructions")).isNotNull();
        assertThat(json.get("transactions")).isNotNull();
        assertThat(json.get("categories")).isNotNull();
        assertThat(json.get("expectedResponseFormat")).isNotNull();

        var transactions = json.get("transactions");
        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).get("transactionId").asText())
                .isEqualTo(transaction.transactionId().value().toString());
        assertThat(transactions.get(0).get("description").asText())
                .isEqualTo("McDonald's Downtown");
        assertThat(transactions.get(0).get("type").asText()).isEqualTo("EXPENSE");

        var categories = json.get("categories");
        assertThat(categories).hasSize(1);
        assertThat(categories.get(0).get("categoryId").asText())
                .isEqualTo(category.id().value().toString());
        assertThat(categories.get(0).get("name").asText()).isEqualTo("Fast Food");
        assertThat(categories.get(0).get("type").asText()).isEqualTo("EXPENSE");
    }

    @Test
    void shouldAssignCorrectIndicesToTransactions() throws Exception {
        var transaction1 = createTransaction("McDonald's", TransactionType.EXPENSE);
        var transaction2 = createTransaction("Salary", TransactionType.INCOME);
        var transaction3 = createTransaction("Starbucks", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt =
                builder.build(List.of(transaction1, transaction2, transaction3), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var transactions = json.get("transactions");
        assertThat(transactions.size()).isEqualTo(3);
        assertThat(transactions.get(0).get("transactionId").asText())
                .isEqualTo(transaction1.transactionId().value().toString());
        assertThat(transactions.get(1).get("transactionId").asText())
                .isEqualTo(transaction2.transactionId().value().toString());
        assertThat(transactions.get(2).get("transactionId").asText())
                .isEqualTo(transaction3.transactionId().value().toString());
    }

    @Test
    void shouldEscapeSpecialCharactersInDescriptions() throws Exception {
        var transaction =
                createTransaction("\"McDonald's\" - Special\nOffer", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var transactions = json.get("transactions");
        assertThat(transactions.get(0).get("description").asText())
                .isEqualTo("\"McDonald's\" - Special\nOffer");
    }

    @Test
    void shouldIncludeSystemInstructionsInPrompt() throws Exception {
        var transaction = createTransaction("Test", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var instructions = json.get("systemInstructions").asText();
        assertThat(instructions).contains("financial transaction categorization");
        assertThat(instructions).contains("confidence");
    }

    @Test
    void shouldIncludeExpectedResponseFormatInPrompt() throws Exception {
        var transaction = createTransaction("Test", TransactionType.EXPENSE);
        var category = createCategory("Food", CategoryType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var format = json.get("expectedResponseFormat").asText();
        assertThat(format).contains("transactionId");
        assertThat(format).contains("categoryId");
        assertThat(format).contains("confidence");
    }

    private TransactionProposal createTransaction(String description, TransactionType type) {
        return new TransactionProposal(
                new TransactionProposalId(UUID.randomUUID()),
                LocalDate.now(),
                description,
                BigDecimal.valueOf(100),
                Currency.PLN,
                type,
                null);
    }

    @Test
    void shouldBuildHierarchicalCategoryStructureCorrectly() throws Exception {
        var auditInfo = AuditInfo.create(UserId.generate(), GroupId.generate());
        var food = Category.create("Food", CategoryType.EXPENSE, Color.of("#FF0000"), auditInfo);
        var restaurants =
                Category.create(
                        "Restaurants",
                        CategoryType.EXPENSE,
                        Color.of("#FF0000"),
                        food.id(),
                        auditInfo);
        var fastFood =
                Category.create(
                        "Fast Food",
                        CategoryType.EXPENSE,
                        Color.of("#FF0000"),
                        restaurants.id(),
                        auditInfo);
        var transaction = createTransaction("McDonald's", TransactionType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(food, restaurants, fastFood));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var categories = json.get("categories");
        assertThat(categories).hasSize(1);

        var foodNode = categories.get(0);
        assertThat(foodNode.get("categoryId").asText()).isEqualTo(food.id().value().toString());
        assertThat(foodNode.get("name").asText()).isEqualTo("Food");
        assertThat(foodNode.get("children")).hasSize(1);

        var restaurantsNode = foodNode.get("children").get(0);
        assertThat(restaurantsNode.get("categoryId").asText())
                .isEqualTo(restaurants.id().value().toString());
        assertThat(restaurantsNode.get("name").asText()).isEqualTo("Restaurants");
        assertThat(restaurantsNode.get("children")).hasSize(1);

        var fastFoodNode = restaurantsNode.get("children").get(0);
        assertThat(fastFoodNode.get("categoryId").asText())
                .isEqualTo(fastFood.id().value().toString());
        assertThat(fastFoodNode.get("name").asText()).isEqualTo("Fast Food");
        assertThat(fastFoodNode.get("children")).hasSize(0);
    }

    @Test
    void shouldHandleNullCategoryParentIdForRootCategories() throws Exception {
        var category1 = createCategory("Food", CategoryType.EXPENSE);
        var category2 = createCategory("Transport", CategoryType.EXPENSE);
        var transaction = createTransaction("Test", TransactionType.EXPENSE);

        var prompt = builder.build(List.of(transaction), List.of(category1, category2));

        var json = objectMapper.readTree(prompt.jsonPrompt());
        var categories = json.get("categories");
        assertThat(categories).hasSize(2);
    }

    private Category createCategory(String name, CategoryType type) {
        var auditInfo = AuditInfo.create(UserId.generate(), GroupId.generate());
        return Category.create(name, type, Color.of("#FF0000"), auditInfo);
    }
}
