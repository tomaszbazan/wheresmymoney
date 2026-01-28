package pl.btsoftware.backend.csvimport.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class TransactionProposalDescriptionTooLongException extends BusinessException {
    private static final String ERROR_CODE = "TRANSACTION_PROPOSAL_DESCRIPTION_TOO_LONG";
    private static final String MESSAGE = "Description cannot exceed 100 characters";

    public TransactionProposalDescriptionTooLongException() {
        super(ERROR_CODE, MESSAGE);
    }
}
