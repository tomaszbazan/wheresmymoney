package pl.btsoftware.backend.csvimport.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClient;
import pl.btsoftware.backend.category.domain.CategoryRepository;
import pl.btsoftware.backend.shared.CategoryType;
import pl.btsoftware.backend.shared.TransactionType;
import pl.btsoftware.backend.users.domain.GroupId;

@Service
@ConditionalOnBean(GeminiClient.class)
@RequiredArgsConstructor
@Slf4j
public class CategorySuggestionService {
    private final CategoryRepository categoryRepository;
    private final GeminiClient geminiClient;
    private final CategorizationPromptBuilder promptBuilder;
    private final GeminiResponseParser responseParser;

    public List<CategorySuggestion> suggestCategories(List<TransactionProposal> transactions, GroupId groupId) {
        validateInputs(transactions, groupId);

        var transactionsByType = groupTransactionsByType(transactions);

        var allSuggestions = new ArrayList<CategorySuggestion>();

        for (var entry : transactionsByType.entrySet()) {
            var type = entry.getKey();
            var typeTransactions = entry.getValue();
            var suggestions = processByType(typeTransactions, type, groupId);

            if (suggestions == null) {
                return null;
            }

            allSuggestions.addAll(suggestions);
        }

        return allSuggestions;
    }

    private void validateInputs(List<TransactionProposal> transactions, GroupId groupId) {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("Transaction list cannot be null or empty");
        }
        if (groupId == null) {
            throw new IllegalArgumentException("GroupId cannot be null");
        }
    }

    private Map<TransactionType, List<TransactionProposal>> groupTransactionsByType(
            List<TransactionProposal> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(TransactionProposal::type));
    }

    private List<CategorySuggestion> processByType(
            List<TransactionProposal> transactions, TransactionType type, GroupId groupId) {
        var categoryType = mapTransactionTypeToCategory(type);
        var categories = categoryRepository.findByType(categoryType, groupId);

        if (categories.isEmpty()) {
            return List.of();
        }

        try {
            var prompt = promptBuilder.build(transactions, categories);
            var responseFuture = geminiClient.generateContent(prompt.jsonPrompt());
            var response = responseFuture.join();
            log.info("Received category suggestions response from Gemini API: {}", response);
            return responseParser.parse(response);
        } catch (Exception e) {
            log.warn("Failed to get category suggestions from Gemini API: {}", e.getMessage(), e);
            return null;
        }
    }

    private CategoryType mapTransactionTypeToCategory(TransactionType type) {
        return switch (type) {
            case EXPENSE -> CategoryType.EXPENSE;
            case INCOME -> CategoryType.INCOME;
        };
    }
}
