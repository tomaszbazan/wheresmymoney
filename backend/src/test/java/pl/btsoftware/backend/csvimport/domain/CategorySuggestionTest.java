package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.TransactionId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CategorySuggestionTest {

    @Test
    void shouldCreateCategorySuggestionWithValidData() {
        // given
        var transactionId = TransactionId.generate();
        var categoryId = CategoryId.generate();
        var confidence = 0.85;

        // when
        var suggestion = new CategorySuggestion(transactionId, categoryId, confidence);

        // then
        assertThat(suggestion.transactionId()).isEqualTo(transactionId);
        assertThat(suggestion.categoryId()).isEqualTo(categoryId);
        assertThat(suggestion.confidence()).isEqualTo(0.85);
    }

    @Test
    void shouldRejectConfidenceBelowZero() {
        // given
        var transactionId = TransactionId.generate();
        var categoryId = CategoryId.generate();
        var confidence = -0.1;

        // when & then
        assertThatThrownBy(() -> new CategorySuggestion(transactionId, categoryId, confidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void shouldRejectConfidenceAboveOne() {
        // given
        var transactionId = TransactionId.generate();
        var categoryId = CategoryId.generate();
        var confidence = 1.1;

        // when & then
        assertThatThrownBy(() -> new CategorySuggestion(transactionId, categoryId, confidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Confidence must be between 0.0 and 1.0");
    }
}
