package pl.btsoftware.backend.shared;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.shared.Currency.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.shared.error.InvalidExchangeRateException;

class ExchangeRateTest {

    @Test
    void shouldCreateIdentityRateForSameCurrency() {
        // when
        var rate = ExchangeRate.identity(PLN);

        // then
        assertThat(rate.rate()).isEqualByComparingTo(ONE);
        assertThat(rate.fromCurrency()).isEqualTo(PLN);
        assertThat(rate.toCurrency()).isEqualTo(PLN);
    }

    @Test
    void shouldCalculateRateFromSourceAndTargetAmounts() {
        // given
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("85.50"), EUR);

        // when
        var rate = ExchangeRate.calculate(sourceAmount, targetAmount);

        // then
        assertThat(rate.fromCurrency()).isEqualTo(PLN);
        assertThat(rate.toCurrency()).isEqualTo(EUR);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("0.855"));
    }

    @Test
    void shouldRoundRateTo6DecimalPlaces() {
        // given
        var sourceAmount = Money.of(new BigDecimal("100000.00"), PLN);
        var targetAmount = Money.of(new BigDecimal("33.33"), EUR);

        // when
        var rate = ExchangeRate.calculate(sourceAmount, targetAmount);

        // then
        assertThat(rate.rate().scale()).isEqualTo(6);
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("0.000333"));
    }

    @Test
    void shouldConvertMoneyUsingRate() {
        // given
        var rate = new ExchangeRate(new BigDecimal("0.855000"), PLN, EUR);
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);

        // when
        var converted = rate.convert(sourceAmount);

        // then
        assertThat(converted.currency()).isEqualTo(EUR);
        assertThat(converted.value()).isEqualByComparingTo(new BigDecimal("85.50"));
    }

    @Test
    void shouldRejectNegativeRate() {
        // when & then
        assertThatThrownBy(() -> new ExchangeRate(new BigDecimal("-1.0"), PLN, EUR))
                .isInstanceOf(InvalidExchangeRateException.class);
    }

    @Test
    void shouldRejectZeroRate() {
        // when & then
        assertThatThrownBy(() -> new ExchangeRate(ZERO, PLN, EUR))
                .isInstanceOf(InvalidExchangeRateException.class);
    }

    @Test
    void shouldRejectNullRate() {
        // when & then
        assertThatThrownBy(() -> new ExchangeRate(null, PLN, EUR))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullFromCurrency() {
        // when & then
        assertThatThrownBy(() -> new ExchangeRate(ONE, null, EUR))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectNullToCurrency() {
        // when & then
        assertThatThrownBy(() -> new ExchangeRate(ONE, PLN, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldConvertWithIdentityRate() {
        // given
        var rate = ExchangeRate.identity(PLN);
        var sourceAmount = Money.of(new BigDecimal("100.00"), PLN);

        // when
        var converted = rate.convert(sourceAmount);

        // then
        assertThat(converted.currency()).isEqualTo(PLN);
        assertThat(converted.value()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void shouldHandleLargeExchangeRates() {
        // given
        var sourceAmount = Money.of(new BigDecimal("1.00"), EUR);
        var targetAmount = Money.of(new BigDecimal("145.50"), PLN);

        // when
        var rate = ExchangeRate.calculate(sourceAmount, targetAmount);

        // then
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("145.500000"));
    }

    @Test
    void shouldHandleSmallExchangeRates() {
        // given
        var sourceAmount = Money.of(new BigDecimal("100.00"), GBP);
        var targetAmount = Money.of(new BigDecimal("0.75"), EUR);

        // when
        var rate = ExchangeRate.calculate(sourceAmount, targetAmount);

        // then
        assertThat(rate.rate()).isEqualByComparingTo(new BigDecimal("0.007500"));
    }
}
