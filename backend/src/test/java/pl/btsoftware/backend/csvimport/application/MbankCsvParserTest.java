package pl.btsoftware.backend.csvimport.application;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.csvimport.domain.CsvImportException;
import pl.btsoftware.backend.csvimport.domain.ErrorType;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MbankCsvParserTest {

    private final MbankCsvParser parser = new MbankCsvParser();

    @Test
    void shouldParseValidCsvFromSampleFile() {
        // given
        var csvStream = getClass().getClassLoader().getResourceAsStream("mbank_transaction_list.csv");

        assertThat(csvStream).isNotNull();

        // when
        var result = parser.parse(csvStream, Currency.PLN);

        // then
        assertThat(result.proposals()).hasSize(9);
        assertThat(result.successCount()).isEqualTo(9);
        assertThat(result.errorCount()).isZero();
        assertThat(result.errors()).isEmpty();
    }

    @Test
    void shouldParseIncomeTransaction() {
        // given
        var csvStream = getClass().getClassLoader().getResourceAsStream("mbank_transaction_list.csv");

        // when
        var result = parser.parse(csvStream, Currency.PLN);

        // then
        var incomeProposal = result.proposals().stream().filter(p -> p.type() == TransactionType.INCOME).findFirst().orElseThrow();

        assertThat(incomeProposal.type()).isEqualTo(TransactionType.INCOME);
        assertThat(incomeProposal.amount()).isEqualTo(new BigDecimal("1100.00"));
        assertThat(incomeProposal.currency()).isEqualTo(Currency.PLN);
        assertThat(incomeProposal.categoryId()).isNull();
        assertThat(incomeProposal.description()).contains(" / ");
    }

    @Test
    void shouldParseExpenseTransaction() {
        // given
        var csvStream = getClass().getClassLoader().getResourceAsStream("mbank_transaction_list.csv");

        // when
        var result = parser.parse(csvStream, Currency.PLN);

        // then
        var expenseProposal = result.proposals().stream().filter(p -> p.type() == TransactionType.EXPENSE).findFirst().orElseThrow();

        assertThat(expenseProposal.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(expenseProposal.amount()).isLessThan(BigDecimal.ZERO);
        assertThat(expenseProposal.currency()).isEqualTo(Currency.PLN);
        assertThat(expenseProposal.categoryId()).isNull();
        assertThat(expenseProposal.description()).contains(" / ");
    }

    @Test
    void shouldRejectEmptyFile() {
        // given
        var stream = createInputStream("");

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("empty");
    }

    @Test
    void shouldRejectFileTooShort() {
        // given
        var stream = getClass().getClassLoader().getResourceAsStream("too_short.csv");

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("at least 28 lines");
    }

    @Test
    void shouldRejectMissingColumnHeaders() {
        // given
        var content = createValidHeaderLines(26) + "\n";
        var stream = createInputStream(content);

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("column headers");
    }

    @Test
    void shouldRejectIncorrectColumnHeaders() {
        // given
        var stream = getClass().getClassLoader().getResourceAsStream("invalid_headers.csv");

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("Expected mBank column headers");
    }

    @Test
    void shouldRejectMissingRequiredColumn() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;\n"
                      + "\n";
        var stream = createInputStream(content);

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("Expected mBank column headers");
    }

    @Test
    void shouldRejectWrongColumnOrder() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Opis operacji;#Data operacji;#Rachunek;#Kategoria;#Kwota;\n"
                      + "\n";
        var stream = createInputStream(content);

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("Expected mBank column headers");
    }

    @Test
    void shouldRejectWrongDelimiter() {
        // given
        var stream = getClass().getClassLoader().getResourceAsStream("wrong_delimiter.csv");

        // when & then
        assertThatThrownBy(() -> parser.parse(stream, Currency.PLN))
                .isInstanceOf(CsvImportException.class)
                .hasMessageContaining("Expected mBank column headers");
    }

    @Test
    void shouldReturnInvalidDateFormatErrorTypeForInvalidDate() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;\n"
                      + "invalid-date;Test description;Account;Category;100.00 PLN\n";
        var stream = createInputStream(content);

        // when
        var result = parser.parse(stream, Currency.PLN);

        // then
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst().type()).isEqualTo(ErrorType.INVALID_DATE_FORMAT);
    }

    @Test
    void shouldReturnInvalidAmountFormatErrorTypeForInvalidAmount() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;\n"
                      + "2024-01-15;Test description;Account;Category; PLN\n";
        var stream = createInputStream(content);

        // when
        var result = parser.parse(stream, Currency.PLN);

        // then
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst().type()).isEqualTo(ErrorType.INVALID_AMOUNT_FORMAT);
    }

    @Test
    void shouldReturnInvalidCurrencyErrorTypeForUnsupportedCurrency() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;\n"
                      + "2024-01-15;Test description;Account;Category;100.00 XYZ\n";
        var stream = createInputStream(content);

        // when
        var result = parser.parse(stream, Currency.PLN);

        // then
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst().type()).isEqualTo(ErrorType.INVALID_CURRENCY);
    }

    @Test
    void shouldReturnCurrencyMismatchErrorTypeForDifferentCurrency() {
        // given
        var content = createValidHeaderLines(26)
                      + "#Data operacji;#Opis operacji;#Rachunek;#Kategoria;#Kwota;\n"
                      + "2024-01-15;Test description;Account;Category;100.00 EUR\n";
        var stream = createInputStream(content);

        // when
        var result = parser.parse(stream, Currency.PLN);

        // then
        assertThat(result.errorCount()).isEqualTo(1);
        assertThat(result.errors()).hasSize(1);
        assertThat(result.errors().getFirst().type()).isEqualTo(ErrorType.CURRENCY_MISMATCH);
    }

    private InputStream createInputStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private String createValidHeaderLines(int count) {
        var builder = new StringBuilder();
        for (var i = 0; i < count; i++) {
            builder.append("Header line ").append(i + 1).append(";\n");
        }
        return builder.toString();
    }
}
