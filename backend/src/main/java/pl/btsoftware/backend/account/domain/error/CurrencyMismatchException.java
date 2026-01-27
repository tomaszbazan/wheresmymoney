package pl.btsoftware.backend.account.domain.error;

public class CurrencyMismatchException extends BusinessException {
    private static final String ERROR_CODE = "CURRENCY_MISMATCH";
    private static final String MESSAGE =
            "Expense currency (%s) does not match account currency (%s)";

    public CurrencyMismatchException(String expenseCurrency, String accountCurrency) {
        super(ERROR_CODE, MESSAGE.formatted(expenseCurrency, accountCurrency));
    }
}
