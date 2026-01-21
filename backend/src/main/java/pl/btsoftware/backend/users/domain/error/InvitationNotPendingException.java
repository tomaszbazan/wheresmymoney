package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class InvitationNotPendingException extends BusinessException {
    private static final String ERROR_CODE = "INVITATION_NOT_PENDING";
    private static final String MESSAGE = "Invitation is not in pending status";

    public InvitationNotPendingException() {
        super(ERROR_CODE, MESSAGE);
    }
}
