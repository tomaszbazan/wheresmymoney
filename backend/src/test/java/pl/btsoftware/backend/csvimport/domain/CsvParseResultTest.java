package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvParseResultTest {

    @Test
    void shouldCreateResultWithProposalsAndErrors() {
        // given
        var proposal = new TransactionProposal(LocalDate.of(2025, 12, 17), "Wpływy / Test", new BigDecimal("100.00"), Currency.PLN, TransactionType.INCOME, null);
        var error = new ParseError(5, "Invalid format");

        // when
        var result = new CsvParseResult(List.of(proposal), List.of(error), 2, 1, 1);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.totalRows()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.errorCount()).isEqualTo(1);
    }

    @Test
    void shouldCreateResultWithNoErrors() {
        // given
        var proposal = new TransactionProposal(LocalDate.of(2025, 12, 17), "Wpływy / Test", new BigDecimal("100.00"), Currency.PLN, TransactionType.INCOME, null);

        // when
        var result = new CsvParseResult(List.of(proposal), List.of(), 1, 1, 0);

        // then
        assertThat(result.proposals()).hasSize(1);
        assertThat(result.errors()).isEmpty();
        assertThat(result.totalRows()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.errorCount()).isEqualTo(0);
    }

    @Test
    void shouldCreateResultWithNoProposals() {
        // given
        var error = new ParseError(1, "Invalid format");

        // when
        var result = new CsvParseResult(List.of(), List.of(error), 1, 0, 1);

        // then
        assertThat(result.proposals()).isEmpty();
        assertThat(result.errors()).hasSize(1);
        assertThat(result.totalRows()).isEqualTo(1);
        assertThat(result.successCount()).isEqualTo(0);
        assertThat(result.errorCount()).isEqualTo(1);
    }

    @Test
    void shouldCalculateSuccessRate() {
        // given
        var result = new CsvParseResult(List.of(), List.of(), 10, 8, 2);

        // when
        var successRate = result.successRate();

        // then
        assertThat(successRate).isCloseTo(0.8, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void shouldHandleZeroTotalRows() {
        // given
        var result = new CsvParseResult(List.of(), List.of(), 0, 0, 0);

        // when
        var successRate = result.successRate();

        // then
        assertThat(successRate).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void shouldRejectNullProposals() {
        // when & then
        assertThatThrownBy(() -> new CsvParseResult(null, List.of(), 0, 0, 0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullErrors() {
        // when & then
        assertThatThrownBy(() -> new CsvParseResult(List.of(), null, 0, 0, 0))
                .isInstanceOf(NullPointerException.class);
    }
}
