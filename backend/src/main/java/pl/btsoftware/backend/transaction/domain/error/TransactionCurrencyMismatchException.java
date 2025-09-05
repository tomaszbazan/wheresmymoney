package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.shared.Currency;

public class TransactionCurrencyMismatchException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_CURRENCY_MISMATCH";
    private static final String MESSAGE = "Transaction currency (%s) must match account currency (%s)";

    public TransactionCurrencyMismatchException(Currency transactionCurrency, Currency accountCurrency) {
        super(ERROR_CODE, MESSAGE.formatted(transactionCurrency, accountCurrency));
    }
}