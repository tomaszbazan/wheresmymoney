package pl.btsoftware.backend.csvimport.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParseResultTest {

    @Test
    void shouldCreateResultWithProposalsAndErrors() {
        var proposal = new TransactionProposal(LocalDate.of(2025, 12, 17), "Wpływy / Test", new BigDecimal("100.00"), Currency.PLN, TransactionType.INCOME, null);

        var error = new ParseError(5, "Invalid format");

        var result = new CsvParseResult(List.of(proposal), List.of(error), 2, 1, 1);

        assertEquals(1, result.proposals().size());
        assertEquals(1, result.errors().size());
        assertEquals(2, result.totalRows());
        assertEquals(1, result.successCount());
        assertEquals(1, result.errorCount());
    }

    @Test
    void shouldCreateResultWithNoErrors() {
        var proposal = new TransactionProposal(LocalDate.of(2025, 12, 17), "Wpływy / Test", new BigDecimal("100.00"), Currency.PLN, TransactionType.INCOME, null);

        var result = new CsvParseResult(List.of(proposal), List.of(), 1, 1, 0);

        assertEquals(1, result.proposals().size());
        assertTrue(result.errors().isEmpty());
        assertEquals(1, result.totalRows());
        assertEquals(1, result.successCount());
        assertEquals(0, result.errorCount());
    }

    @Test
    void shouldCreateResultWithNoProposals() {
        var error = new ParseError(1, "Invalid format");

        var result = new CsvParseResult(List.of(), List.of(error), 1, 0, 1);

        assertTrue(result.proposals().isEmpty());
        assertEquals(1, result.errors().size());
        assertEquals(1, result.totalRows());
        assertEquals(0, result.successCount());
        assertEquals(1, result.errorCount());
    }

    @Test
    void shouldCalculateSuccessRate() {
        var result = new CsvParseResult(List.of(), List.of(), 10, 8, 2);

        assertEquals(0.8, result.successRate(), 0.001);
    }

    @Test
    void shouldHandleZeroTotalRows() {
        var result = new CsvParseResult(List.of(), List.of(), 0, 0, 0);

        assertEquals(0.0, result.successRate(), 0.001);
    }

    @Test
    void shouldRejectNullProposals() {
        assertThrows(NullPointerException.class, () -> new CsvParseResult(null, List.of(), 0, 0, 0));
    }

    @Test
    void shouldRejectNullErrors() {
        assertThrows(NullPointerException.class, () -> new CsvParseResult(List.of(), null, 0, 0, 0));
    }
}
