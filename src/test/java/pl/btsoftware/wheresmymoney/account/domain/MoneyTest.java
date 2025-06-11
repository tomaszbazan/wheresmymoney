package pl.btsoftware.wheresmymoney.account.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.wheresmymoney.account.domain.error.InvalidCurrencyException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        // when & then
        assertThrows(NullPointerException.class, () -> new Money(null, "PLN"));
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        // when & then
        assertThrows(NullPointerException.class, () -> new Money(BigDecimal.TEN, null));
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNotSupported() {
        // when & then
        InvalidCurrencyException exception = assertThrows(InvalidCurrencyException.class,
            () -> new Money(BigDecimal.TEN, "JPY"));

        // then
        assertThat(exception.getMessage()).contains("Invalid currency");
        assertThat(exception.getMessage()).contains("PLN");
        assertThat(exception.getMessage()).contains("EUR");
        assertThat(exception.getMessage()).contains("USD");
        assertThat(exception.getMessage()).contains("GBP");
    }

    @Test
    void shouldAcceptAllSupportedCurrencies() {
        // when & then - no exceptions should be thrown
        Money pln = new Money(BigDecimal.TEN, "PLN");
        Money eur = new Money(BigDecimal.TEN, "EUR");
        Money usd = new Money(BigDecimal.TEN, "USD");
        Money gbp = new Money(BigDecimal.TEN, "GBP");

        // then
        assertThat(pln.currency()).isEqualTo("PLN");
        assertThat(eur.currency()).isEqualTo("EUR");
        assertThat(usd.currency()).isEqualTo("USD");
        assertThat(gbp.currency()).isEqualTo("GBP");
    }

    @Test
    void shouldScaleAmountToTwoDecimalPlaces() {
        // given
        BigDecimal amount = new BigDecimal("10.123");

        // when
        Money money = new Money(amount, "PLN");

        // then
        assertThat(money.amount()).isEqualByComparingTo(new BigDecimal("10.12"));
        assertEquals(2, money.amount().scale());
    }

    @Test
    void shouldCreateMoneyWithDefaultCurrency() {
        // when
        Money money = Money.of(BigDecimal.TEN);

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(money.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldCreateMoneyWithSpecifiedCurrency() {
        // when
        Money money = Money.of(BigDecimal.TEN, "USD");

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(money.currency()).isEqualTo("USD");
    }

    @Test
    void shouldCreateZeroMoney() {
        // when
        Money money = Money.zero();

        // then
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        // given
        Money money1 = Money.of(new BigDecimal("10.50"));
        Money money2 = Money.of(new BigDecimal("5.25"));

        // when
        Money result = money1.add(money2);

        // then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("15.75"));
        assertThat(result.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        // given
        Money money1 = Money.of(BigDecimal.TEN, "PLN");
        Money money2 = Money.of(BigDecimal.ONE, "USD");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> money1.add(money2));
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        // given
        Money money1 = Money.of(new BigDecimal("10.50"));
        Money money2 = Money.of(new BigDecimal("5.25"));

        // when
        Money result = money1.subtract(money2);

        // then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("5.25"));
        assertThat(result.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowExceptionWhenSubtractingDifferentCurrencies() {
        // given
        Money money1 = Money.of(BigDecimal.TEN, "PLN");
        Money money2 = Money.of(BigDecimal.ONE, "USD");

        // when & then
        assertThrows(IllegalArgumentException.class, () -> money1.subtract(money2));
    }

    @Test
    void shouldMultiplyMoney() {
        // given
        Money money = Money.of(new BigDecimal("10.50"));
        BigDecimal factor = new BigDecimal("2.5");

        // when
        Money result = money.multiply(factor);

        // then
        assertThat(result.amount()).isEqualByComparingTo(new BigDecimal("26.25"));
        assertThat(result.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldChangeCurrency() {
        // given
        Money money = Money.of(BigDecimal.TEN);

        // when
        Money result = money.withCurrency("USD");

        // then
        assertThat(result.amount()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.currency()).isEqualTo("USD");
        // Original balance should be unchanged (immutability check)
        assertThat(money.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldThrowExceptionWhenChangingToUnsupportedCurrency() {
        // given
        Money money = Money.of(BigDecimal.TEN);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> money.withCurrency("JPY"));

        // then
        assertThat(exception.getMessage()).contains("Unsupported currency");
        assertThat(exception.getMessage()).contains("JPY");
    }

    @Test
    void shouldChangeAmount() {
        // given
        Money money = Money.of(BigDecimal.TEN);
        BigDecimal newAmount = new BigDecimal("20.50");

        // when
        Money result = money.withAmount(newAmount);

        // then
        assertThat(result.amount()).isEqualByComparingTo(newAmount);
        assertThat(result.currency()).isEqualTo("PLN");
        // Original balance should be unchanged (immutability check)
        assertThat(money.amount()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void shouldFormatToString() {
        // given
        Money money = Money.of(new BigDecimal("10.50"), "USD");

        // when
        String result = money.toString();

        // then
        assertThat(result).isEqualTo("10.50 USD");
    }
}
