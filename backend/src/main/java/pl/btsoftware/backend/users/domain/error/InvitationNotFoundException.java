package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class InvitationNotFoundException extends BusinessException {
    public InvitationNotFoundException() {
        super("INVITATION_NOT_FOUND", "Invitation not found");
    }
}