package pl.btsoftware.backend.shared.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.Currency;

public class MoneyCurrencyMismatchException extends BusinessException {
    private static final String ERROR_CODE = "MONEY_CURRENCY_MISMATCH";
    private static final String MESSAGE_ADD = "Cannot add money with different currencies: %s and %s";
    private static final String MESSAGE_SUBTRACT = "Cannot subtract money with different currencies: %s and %s";

    private MoneyCurrencyMismatchException(String message) {
        super(ERROR_CODE, message);
    }

    public static MoneyCurrencyMismatchException forAdd(Currency currency1, Currency currency2) {
        return new MoneyCurrencyMismatchException(MESSAGE_ADD.formatted(currency1, currency2));
    }

    public static MoneyCurrencyMismatchException forSubtract(Currency currency1, Currency currency2) {
        return new MoneyCurrencyMismatchException(MESSAGE_SUBTRACT.formatted(currency1, currency2));
    }
}
