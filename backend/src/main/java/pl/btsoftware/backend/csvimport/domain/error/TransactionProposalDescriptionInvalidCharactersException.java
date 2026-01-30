package pl.btsoftware.backend.csvimport.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionProposalDescriptionInvalidCharactersException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_PROPOSAL_DESCRIPTION_INVALID_CHARACTERS";
    private static final String MESSAGE = "Transaction proposal description contains invalid characters";

    public TransactionProposalDescriptionInvalidCharactersException() {
        super(ERROR_CODE, MESSAGE);
    }
}
