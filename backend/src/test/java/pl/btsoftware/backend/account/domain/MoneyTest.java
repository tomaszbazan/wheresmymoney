package pl.btsoftware.backend.account.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.Money;
import pl.btsoftware.backend.shared.error.MoneyCurrencyMismatchException;

class MoneyTest {

    @Test
    void shouldThrowExceptionWhenAmountIsNull() {
        // when & then
        assertThatThrownBy(() -> new Money(null, PLN)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldThrowExceptionWhenCurrencyIsNull() {
        // when & then
        assertThatThrownBy(() -> new Money(BigDecimal.TEN, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAcceptAllSupportedCurrencies() {
        // when & then - no exceptions should be thrown
        Money pln = new Money(BigDecimal.TEN, PLN);
        Money eur = new Money(BigDecimal.TEN, EUR);
        Money usd = new Money(BigDecimal.TEN, USD);
        Money gbp = new Money(BigDecimal.TEN, GBP);

        // then
        assertThat(pln.currency()).isEqualTo(PLN);
        assertThat(eur.currency()).isEqualTo(EUR);
        assertThat(usd.currency()).isEqualTo(USD);
        assertThat(gbp.currency()).isEqualTo(GBP);
    }

    @Test
    void shouldScaleAmountToTwoDecimalPlaces() {
        // given
        BigDecimal amount = new BigDecimal("10.123");

        // when
        Money money = new Money(amount, PLN);

        // then
        assertThat(money.value()).isEqualByComparingTo(new BigDecimal("10.12"));
        assertThat(money.value().scale()).isEqualTo(2);
    }

    @Test
    void shouldCreateMoneyWithDefaultCurrency() {
        // when
        Money money = Money.of(BigDecimal.TEN);

        // then
        assertThat(money.value()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(money.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldCreateMoneyWithSpecifiedCurrency() {
        // when
        Money money = Money.of(BigDecimal.TEN, USD);

        // then
        assertThat(money.value()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(money.currency()).isEqualTo(USD);
    }

    @Test
    void shouldCreateZeroMoney() {
        // when
        Money money = Money.zero();

        // then
        assertThat(money.value()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(money.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldAddMoneyWithSameCurrency() {
        // given
        Money money1 = Money.of(new BigDecimal("10.50"));
        Money money2 = Money.of(new BigDecimal("5.25"));

        // when
        Money result = money1.add(money2);

        // then
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("15.75"));
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldThrowExceptionWhenAddingDifferentCurrencies() {
        // given
        Money money1 = Money.of(BigDecimal.TEN, PLN);
        Money money2 = Money.of(BigDecimal.ONE, USD);

        // when & then
        assertThatThrownBy(() -> money1.add(money2)).isInstanceOf(MoneyCurrencyMismatchException.class);
    }

    @Test
    void shouldSubtractMoneyWithSameCurrency() {
        // given
        Money money1 = Money.of(new BigDecimal("10.50"));
        Money money2 = Money.of(new BigDecimal("5.25"));

        // when
        Money result = money1.subtract(money2);

        // then
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("5.25"));
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldThrowExceptionWhenSubtractingDifferentCurrencies() {
        // given
        Money money1 = Money.of(BigDecimal.TEN, PLN);
        Money money2 = Money.of(BigDecimal.ONE, USD);

        // when & then
        assertThatThrownBy(() -> money1.subtract(money2)).isInstanceOf(MoneyCurrencyMismatchException.class);
    }

    @Test
    void shouldMultiplyMoney() {
        // given
        Money money = Money.of(new BigDecimal("10.50"));
        BigDecimal factor = new BigDecimal("2.5");

        // when
        Money result = money.multiply(factor);

        // then
        assertThat(result.value()).isEqualByComparingTo(new BigDecimal("26.25"));
        assertThat(result.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldChangeCurrency() {
        // given
        Money money = Money.of(BigDecimal.TEN);

        // when
        Money result = money.withCurrency(USD);

        // then
        assertThat(result.value()).isEqualByComparingTo(BigDecimal.TEN);
        assertThat(result.currency()).isEqualTo(USD);
        // Original balance should be unchanged (immutability check)
        assertThat(money.currency()).isEqualTo(PLN);
    }

    @Test
    void shouldChangeAmount() {
        // given
        Money money = Money.of(BigDecimal.TEN);
        BigDecimal newAmount = new BigDecimal("20.50");

        // when
        Money result = money.withAmount(newAmount);

        // then
        assertThat(result.value()).isEqualByComparingTo(newAmount);
        assertThat(result.currency()).isEqualTo(PLN);
        // Original balance should be unchanged (immutability check)
        assertThat(money.value()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    void shouldFormatToString() {
        // given
        Money money = Money.of(new BigDecimal("10.50"), USD);

        // when
        String result = money.toString();

        // then
        assertThat(result).isEqualTo("10.50 USD");
    }
}
