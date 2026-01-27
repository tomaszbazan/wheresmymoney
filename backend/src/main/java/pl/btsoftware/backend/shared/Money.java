package pl.btsoftware.backend.shared;

import static java.util.Objects.requireNonNull;
import static pl.btsoftware.backend.shared.Currency.DEFAULT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.jetbrains.annotations.NotNull;
import pl.btsoftware.backend.shared.error.MoneyCurrencyMismatchException;

public record Money(BigDecimal value, Currency currency) {

    public Money {
        requireNonNull(value, "Amount cannot be null");
        requireNonNull(currency, "Currency cannot be null");

        // Ensure value has exactly 2 decimal places
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, DEFAULT);
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, DEFAULT);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw MoneyCurrencyMismatchException.forAdd(this.currency, other.currency);
        }
        return new Money(this.value.add(other.value), this.currency);
    }

    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw MoneyCurrencyMismatchException.forSubtract(this.currency, other.currency);
        }
        return new Money(this.value.subtract(other.value), this.currency);
    }

    public Money multiply(BigDecimal factor) {
        return new Money(this.value.multiply(factor), this.currency);
    }

    public Money withCurrency(Currency newCurrency) {
        return new Money(this.value, newCurrency);
    }

    public Money withAmount(BigDecimal newAmount) {
        return new Money(newAmount, this.currency);
    }

    public Money negate() {
        return new Money(value.negate(), currency);
    }

    @NotNull @Override
    public String toString() {
        return value.toString() + " " + currency;
    }
}
