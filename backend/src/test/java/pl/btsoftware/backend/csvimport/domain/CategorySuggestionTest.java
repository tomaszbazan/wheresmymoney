package pl.btsoftware.backend.csvimport.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;

class CategorySuggestionTest {

    @Test
    void shouldCreateCategorySuggestionWithValidData() {
        // given
        var transactionProposalId = TransactionProposalId.generate();
        var categoryId = CategoryId.generate();
        var confidence = 0.85;

        // when
        var suggestion = new CategorySuggestion(transactionProposalId, categoryId, confidence);

        // then
        assertThat(suggestion.transactionProposalId()).isEqualTo(transactionProposalId);
        assertThat(suggestion.categoryId()).isEqualTo(categoryId);
        assertThat(suggestion.confidence()).isEqualTo(0.85);
    }

    @Test
    void shouldRejectConfidenceBelowZero() {
        // given
        var transactionProposalId = TransactionProposalId.generate();
        var categoryId = CategoryId.generate();
        var confidence = -0.1;

        // when & then
        assertThatThrownBy(
                        () -> new CategorySuggestion(transactionProposalId, categoryId, confidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Confidence must be between 0.0 and 1.0");
    }

    @Test
    void shouldRejectConfidenceAboveOne() {
        // given
        var transactionProposalId = TransactionProposalId.generate();
        var categoryId = CategoryId.generate();
        var confidence = 1.1;

        // when & then
        assertThatThrownBy(
                        () -> new CategorySuggestion(transactionProposalId, categoryId, confidence))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Confidence must be between 0.0 and 1.0");
    }
}
