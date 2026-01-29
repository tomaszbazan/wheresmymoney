package pl.btsoftware.backend.transaction.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.AccountId;
import pl.btsoftware.backend.shared.Currency;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.TransactionType;

class TransactionHashTransactionHashCalculatorTest {

    @Test
    void shouldCalculateSameHashForIdenticalInputs() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(accountId, amount, description, date, type);
        var hash2 =
                TransactionHashCalculator.calculateHash(accountId, amount, description, date, type);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldCalculateSameHashForDescriptionWithDifferentCase() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "Test Transaction", date, type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "test transaction", date, type);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldCalculateSameHashForDescriptionWithDifferentWhitespace() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "  Test transaction  ", date, type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "Test transaction", date, type);

        // then
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentAmount() {
        // given
        var accountId = AccountId.generate();
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId,
                        Money.of(new BigDecimal("100.50"), Currency.PLN),
                        description,
                        date,
                        type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId,
                        Money.of(new BigDecimal("200.50"), Currency.PLN),
                        description,
                        date,
                        type);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentCurrency() {
        // given
        var accountId = AccountId.generate();
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId,
                        Money.of(new BigDecimal("100.50"), Currency.PLN),
                        description,
                        date,
                        type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId,
                        Money.of(new BigDecimal("100.50"), Currency.EUR),
                        description,
                        date,
                        type);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentDate() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var description = "Test transaction";
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, description, LocalDate.of(2024, 1, 15), type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, description, LocalDate.of(2024, 1, 16), type);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentAccountId() {
        // given
        var accountId1 = AccountId.generate();
        var accountId2 = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId1, amount, description, date, type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId2, amount, description, date, type);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentType() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, description, date, TransactionType.EXPENSE);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, description, date, TransactionType.INCOME);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldCalculateDifferentHashForDifferentDescription() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash1 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "Transaction A", date, type);
        var hash2 =
                TransactionHashCalculator.calculateHash(
                        accountId, amount, "Transaction B", date, type);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldProduceHashWith64HexadecimalCharacters() {
        // given
        var accountId = AccountId.generate();
        var amount = Money.of(new BigDecimal("100.50"), Currency.PLN);
        var description = "Test transaction";
        var date = LocalDate.of(2024, 1, 15);
        var type = TransactionType.EXPENSE;

        // when
        var hash =
                TransactionHashCalculator.calculateHash(accountId, amount, description, date, type);

        // then
        assertThat(hash.value()).hasSize(64);
    }
}
