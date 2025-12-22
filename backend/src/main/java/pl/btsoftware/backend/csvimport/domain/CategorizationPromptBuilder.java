package pl.btsoftware.backend.csvimport.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.btsoftware.backend.category.domain.Category;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.TransactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class CategorizationPromptBuilder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CategorizationPrompt build(List<TransactionProposal> transactions, List<Category> categories) {
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("Transaction list cannot be empty");
        }
        if (categories.isEmpty()) {
            throw new IllegalArgumentException("Category list cannot be empty");
        }

        var promptStructure = new PromptStructure(
                buildSystemInstructions(),
                buildTransactions(transactions),
                buildCategoryTree(categories),
                buildExpectedResponseFormat()
        );

        return new CategorizationPrompt(toJson(promptStructure));
    }

    private String buildSystemInstructions() {
        return """
                You are a financial transaction categorization AI. Your task is to suggest appropriate categories \
                for transactions based on their description and type.
                
                Rules:
                1. Match transaction description to the most specific category available
                2. Consider transaction type (INCOME/EXPENSE) when selecting categories
                3. Only suggest categories that match the transaction type
                4. Provide confidence score between 0.0 and 1.0
                5. If uncertain, choose the most general matching category with lower confidence
                6. Return null categoryId if no suitable category exists
                7. Use the exact categoryId from the provided category tree
                
                Response format:
                Return a JSON array of objects, one for each transaction, with the following structure:
                [
                  {
                    "transactionId": <transactionId>,
                    "categoryId": "<uuid>",
                    "confidence": <0.0-1.0>
                  }
                ]""";
    }

    private String buildExpectedResponseFormat() {
        return """
                [
                  {
                    "transactionId": "90185632-c640-476a-8a16-57abbcd777ee",
                    "categoryId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "confidence": 0.95
                  }
                ]""";
    }

    private List<TransactionForPrompt> buildTransactions(List<TransactionProposal> transactions) {
        var result = new ArrayList<TransactionForPrompt>();
        for (int i = 0; i < transactions.size(); i++) {
            var transaction = transactions.get(i);
            result.add(new TransactionForPrompt(
                    i,
                    transaction.description(),
                    transaction.type()
            ));
        }
        return result;
    }

    private List<CategoryNode> buildCategoryTree(List<Category> categories) {
        var categoryMap = new HashMap<CategoryId, CategoryNode>();
        var roots = new ArrayList<CategoryNode>();

        for (var category : categories) {
            var node = new CategoryNode(
                    category.id().value().toString(),
                    category.name(),
                    category.type(),
                    new ArrayList<>()
            );
            categoryMap.put(category.id(), node);
        }

        for (var category : categories) {
            var node = categoryMap.get(category.id());
            if (category.parentId() == null) {
                roots.add(node);
            } else {
                var parent = categoryMap.get(category.parentId());
                if (parent != null) {
                    parent.children().add(node);
                }
            }
        }

        return roots;
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize prompt to JSON", e);
        }
    }

    private record PromptStructure(
            String systemInstructions,
            List<TransactionForPrompt> transactions,
            List<CategoryNode> categories,
            String expectedResponseFormat
    ) {
    }

    private record TransactionForPrompt(
            int transactionId,
            String description,
            TransactionType type
    ) {
    }

    private record CategoryNode(
            String id,
            String name,
            CategoryType type,
            List<CategoryNode> children
    ) {
    }
}
