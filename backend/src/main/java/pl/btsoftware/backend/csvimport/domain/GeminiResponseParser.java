package pl.btsoftware.backend.csvimport.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClientException;
import pl.btsoftware.backend.shared.CategoryId;

@Service
@Slf4j
public class GeminiResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<CategorySuggestion> parse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isBlank()) {
            throw new IllegalArgumentException("JSON response cannot be null or blank");
        }

        try {
            var rootNode = objectMapper.readTree(jsonResponse);
            if (!rootNode.isArray()) {
                throw new GeminiClientException("Response is not a JSON array");
            }

            var suggestions = new ArrayList<CategorySuggestion>();
            for (var node : rootNode) {
                processNode(node, suggestions);
            }

            return suggestions;
        } catch (JsonProcessingException e) {
            throw new GeminiClientException("Failed to parse Gemini response", e);
        }
    }

    private void processNode(JsonNode node, List<CategorySuggestion> suggestions) {
        try {
            if (!node.has("transactionId") || !node.has("confidence")) {
                log.warn("Skipping suggestion missing required fields: {}", node);
                return;
            }

            var transactionId = UUID.fromString(node.get("transactionId").asText());

            double confidence = node.get("confidence").asDouble();

            CategoryId categoryId = null;
            if (node.has("categoryId") && !node.get("categoryId").isNull()) {
                var uuidStr = node.get("categoryId").asText();
                categoryId = CategoryId.of(UUID.fromString(uuidStr));
            }

            suggestions.add(new CategorySuggestion(TransactionProposalId.from(transactionId), categoryId, confidence));
        } catch (IllegalArgumentException e) {
            log.warn("Skipping invalid suggestion: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("Unexpected error processing suggestion node: {}", e.getMessage(), e);
        }
    }
}
