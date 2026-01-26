package pl.btsoftware.backend.shared;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static java.util.Objects.requireNonNull;

import java.math.BigDecimal;
import pl.btsoftware.backend.shared.error.InvalidExchangeRateException;

public record ExchangeRate(BigDecimal rate, Currency fromCurrency, Currency toCurrency) {
    private static final int SCALE = 6;

    public ExchangeRate {
        requireNonNull(rate, "Rate cannot be null");
        requireNonNull(fromCurrency, "From currency cannot be null");
        requireNonNull(toCurrency, "To currency cannot be null");

        if (rate.compareTo(ZERO) <= 0) {
            throw new InvalidExchangeRateException();
        }

        rate = rate.setScale(SCALE, HALF_UP);
    }

    public static ExchangeRate identity(Currency currency) {
        return new ExchangeRate(ONE, currency, currency);
    }

    public static ExchangeRate calculate(Money sourceAmount, Money targetAmount) {
        var rate = targetAmount.value().divide(sourceAmount.value(), SCALE, HALF_UP);
        return new ExchangeRate(rate, sourceAmount.currency(), targetAmount.currency());
    }

    public Money convert(Money source) {
        var convertedValue = source.value().multiply(rate).setScale(2, HALF_UP);
        return Money.of(convertedValue, toCurrency);
    }
}
