package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.ai.infrastructure.client.GeminiClientException;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeminiResponseParserTest {

    private final GeminiResponseParser parser = new GeminiResponseParser();

    @Test
    void shouldParseValidJsonArrayToListOfCategorySuggestions() {
        // given
        var transactionId1 = UUID.randomUUID().toString();
        var transactionId2 = UUID.randomUUID().toString();
        var categoryId1 = UUID.randomUUID().toString();
        var categoryId2 = UUID.randomUUID().toString();
        var jsonResponse = """
                [
                  {"transactionId": "%s", "categoryId": "%s", "confidence": 0.95},
                  {"transactionId": "%s", "categoryId": "%s", "confidence": 0.85}
                ]
                """.formatted(transactionId1, categoryId1, transactionId2, categoryId2);

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).hasSize(2);
        var first = result.getFirst();
        assertThat(first.transactionId()).isEqualTo(TransactionId.of(UUID.fromString(transactionId1)));
        assertThat(first.categoryId()).isEqualTo(CategoryId.of(UUID.fromString(categoryId1)));
        assertThat(first.confidence()).isEqualTo(0.95);

        var last = result.get(1);
        assertThat(last.transactionId()).isEqualTo(TransactionId.of(UUID.fromString(transactionId2)));
        assertThat(last.categoryId()).isEqualTo(CategoryId.of(UUID.fromString(categoryId2)));
        assertThat(last.confidence()).isEqualTo(0.85);
    }

    @Test
    void shouldHandleNullCategoryIdInSuggestion() {
        // given
        var jsonResponse = """
                [
                  {"transactionId": "6c8f5829-c1a0-4dd2-b7ad-6c6ef8deb152", "categoryId": null, "confidence": 0.5}
                ]
                """;

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().transactionId()).isEqualTo(TransactionId.of(UUID.fromString("6c8f5829-c1a0-4dd2-b7ad-6c6ef8deb152")));
        assertThat(result.getFirst().categoryId()).isNull();
        assertThat(result.getFirst().confidence()).isEqualTo(0.5);
    }

    @Test
    void shouldThrowExceptionForMalformedJson() {
        // given
        var jsonResponse = "[{invalid json syntax";

        // when & then
        assertThatThrownBy(() -> parser.parse(jsonResponse))
                .isInstanceOf(GeminiClientException.class)
                .hasMessageContaining("Failed to parse Gemini response");
    }

    @Test
    void shouldSkipSuggestionWithInvalidConfidenceRange() {
        // given
        var uuid = UUID.randomUUID().toString();
        var jsonResponse = """
                [
                  {"transactionId": 0, "categoryId": "%s", "confidence": 1.5}
                ]
                """.formatted(uuid);

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSkipSuggestionWithInvalidCategoryIdFormat() {
        // given
        var jsonResponse = """
                [
                  {"transactionId": "2cb3858b-006d-48c5-8beb-cd76ea18fc98", "categoryId": "not-a-uuid", "confidence": 0.9}
                ]
                """;

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldSkipSuggestionWithInvalidTransactionIdFormat() {
        // given
        var jsonResponse = """
                [
                  {"transactionId": "not-a-uuid", "categoryId": "c10263ae-1174-43be-94ca-c36fb4d37712", "confidence": 0.9}
                ]
                """;

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnOnlyValidSuggestionsWhenMixedValidInvalid() {
        // given
        var uuid = UUID.randomUUID().toString();
        var jsonResponse = """
                [
                  {"transactionId": "6f47f47a-282c-419d-b9c5-22d6b0007fd3", "categoryId": "%s", "confidence": 0.9},
                  {"transactionId": "626b6993-cb32-43c7-9a0c-1a804e4c20b4", "categoryId": "invalid-uuid", "confidence": 0.9},
                  {"transactionId": "30e33e15-3ccb-489e-8e87-828c3ffe626b", "categoryId": "%s", "confidence": 1.5}
                ]
                """.formatted(uuid, uuid);

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().transactionId()).isEqualTo(TransactionId.of(UUID.fromString("6f47f47a-282c-419d-b9c5-22d6b0007fd3")));
    }

    @Test
    void shouldReturnEmptyListForEmptyJsonArray() {
        // given
        var jsonResponse = "[]";

        // when
        List<CategorySuggestion> result = parser.parse(jsonResponse);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void shouldThrowExceptionForNullInput() {
        // when & then
        assertThatThrownBy(() -> parser.parse(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionForBlankInput() {
        // when & then
        assertThatThrownBy(() -> parser.parse("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
