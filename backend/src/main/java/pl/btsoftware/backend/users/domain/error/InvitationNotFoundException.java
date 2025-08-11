package pl.btsoftware.backend.users.domain.error;

public class InvitationNotFoundException extends UserBusinessException {
    public InvitationNotFoundException() {
        super("Invitation not found");
    }
}