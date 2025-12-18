package pl.btsoftware.backend.csvimport.application;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.TransactionType;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MbankCsvParserTest {

    private final MbankCsvParser parser = new MbankCsvParser();

    @Test
    void shouldParseValidCsvFromSampleFile() {
        // given
        var csvStream = getClass().getClassLoader().getResourceAsStream("mbank_transaction_list.csv");

        assertThat(csvStream).isNotNull();

        // when
        var result = parser.parse(csvStream);

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
        var result = parser.parse(csvStream);

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
        var result = parser.parse(csvStream);

        // then
        var expenseProposal = result.proposals().stream().filter(p -> p.type() == TransactionType.EXPENSE).findFirst().orElseThrow();

        assertThat(expenseProposal.type()).isEqualTo(TransactionType.EXPENSE);
        assertThat(expenseProposal.amount()).isLessThan(BigDecimal.ZERO);
        assertThat(expenseProposal.currency()).isEqualTo(Currency.PLN);
        assertThat(expenseProposal.categoryId()).isNull();
        assertThat(expenseProposal.description()).contains(" / ");
    }
}
