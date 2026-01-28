package pl.btsoftware.backend.csvimport.domain;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.CategoryId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

class TransactionProposalTest {

    @Test
    void shouldRemoveInvalidCharactersFromDescription() {
        // given
        var invalidDescription = "Test<script>alert('xss')</script>";

        // when
        var proposal = createProposal(invalidDescription);

        // then
        assertThat(proposal.description()).isEqualTo("Testscriptalertxssscript");
    }

    @Test
    void shouldKeepValidCharactersInDescription() {
        // given
        var validDescription = "Zakupy Biedronka 123 @#!?:,.-_";

        // when
        var proposal = createProposal(validDescription);

        // then
        assertThat(proposal.description()).isEqualTo(validDescription);
    }

    @Test
    void shouldRemoveOnlyInvalidCharactersFromMixedDescription() {
        // given
        var mixedDescription = "Test{value}[array]<tag>";

        // when
        var proposal = createProposal(mixedDescription);

        // then
        assertThat(proposal.description()).isEqualTo("Testvaluearraytag");
    }

    @Test
    void shouldHandlePolishCharactersCorrectly() {
        // given
        var polishDescription = "Zakupy w sklepie Żabka ąćęłńóśźż";

        // when
        var proposal = createProposal(polishDescription);

        // then
        assertThat(proposal.description()).isEqualTo(polishDescription);
    }

    @Test
    void shouldTruncateAndSanitizeDescriptionWhenTooLong() {
        // given
        var longDescription = "a".repeat(150) + "<script>";

        // when
        var proposal = createProposal(longDescription);

        // then
        assertThat(proposal.description()).hasSize(100);
        assertThat(proposal.description()).doesNotContain("<", ">", "script");
    }

    @Test
    void shouldHandleDescriptionWithOnlyInvalidCharacters() {
        // given
        var invalidOnlyDescription = "<>{}[]()";

        // when
        var proposal = createProposal(invalidOnlyDescription);

        // then
        assertThat(proposal.description()).isEmpty();
    }

    @Test
    void shouldHandleNullDescription() {
        // given & when
        var proposal =
                new TransactionProposal(
                        new TransactionProposalId(randomUUID()),
                        LocalDate.now(),
                        null,
                        BigDecimal.TEN,
                        Currency.PLN,
                        TransactionType.EXPENSE,
                        new CategoryId(randomUUID()));

        // then
        assertThat(proposal.description()).isEmpty();
    }

    @Test
    void shouldRejectNullTransactionId() {
        // when & then
        assertThatThrownBy(
                        () ->
                                new TransactionProposal(
                                        null,
                                        LocalDate.now(),
                                        "Test",
                                        BigDecimal.TEN,
                                        Currency.PLN,
                                        TransactionType.EXPENSE,
                                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Transaction id cannot be null");
    }

    @Test
    void shouldRejectNullTransactionDate() {
        // when & then
        assertThatThrownBy(
                        () ->
                                new TransactionProposal(
                                        new TransactionProposalId(randomUUID()),
                                        null,
                                        "Test",
                                        BigDecimal.TEN,
                                        Currency.PLN,
                                        TransactionType.EXPENSE,
                                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Transaction date cannot be null");
    }

    @Test
    void shouldRejectNullAmount() {
        // when & then
        assertThatThrownBy(
                        () ->
                                new TransactionProposal(
                                        new TransactionProposalId(randomUUID()),
                                        LocalDate.now(),
                                        "Test",
                                        null,
                                        Currency.PLN,
                                        TransactionType.EXPENSE,
                                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Amount cannot be null");
    }

    @Test
    void shouldRejectNullCurrency() {
        // when & then
        assertThatThrownBy(
                        () ->
                                new TransactionProposal(
                                        new TransactionProposalId(randomUUID()),
                                        LocalDate.now(),
                                        "Test",
                                        BigDecimal.TEN,
                                        null,
                                        TransactionType.EXPENSE,
                                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Currency cannot be null");
    }

    @Test
    void shouldRejectNullTransactionType() {
        // when & then
        assertThatThrownBy(
                        () ->
                                new TransactionProposal(
                                        new TransactionProposalId(randomUUID()),
                                        LocalDate.now(),
                                        "Test",
                                        BigDecimal.TEN,
                                        Currency.PLN,
                                        null,
                                        null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Transaction type cannot be null");
    }

    private TransactionProposal createProposal(String description) {
        return new TransactionProposal(
                new TransactionProposalId(randomUUID()),
                LocalDate.now(),
                description,
                BigDecimal.TEN,
                Currency.PLN,
                TransactionType.EXPENSE,
                new CategoryId(randomUUID()));
    }
}
