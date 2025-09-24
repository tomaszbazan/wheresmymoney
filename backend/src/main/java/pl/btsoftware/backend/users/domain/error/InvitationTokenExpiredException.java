package pl.btsoftware.backend.users.domain.error;

import pl.btsoftware.backend.account.domain.error.BusinessException;

public class InvitationTokenExpiredException extends BusinessException {
    public InvitationTokenExpiredException() {
        super("INVITATION_TOKEN_EXPIRED", "Invitation token has expired");
    }
}
