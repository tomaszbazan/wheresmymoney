package pl.btsoftware.backend.users.domain.error;

public class InvitationTokenExpiredException extends UserBusinessException {
    public InvitationTokenExpiredException() {
        super("Invitation token has expired");
    }
}