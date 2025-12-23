package pl.btsoftware.backend.transaction.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;
import pl.btsoftware.backend.transaction.domain.TransactionHash;

public class DuplicateTransactionException extends BusinessException {
    private static final String ERROR_CODE = "DUPLICATE_TRANSACTION";

    public DuplicateTransactionException(TransactionHash hash) {
        super(ERROR_CODE, "Transaction with hash " + hash.value() + " already exists (duplicate detected)");
    }
}
