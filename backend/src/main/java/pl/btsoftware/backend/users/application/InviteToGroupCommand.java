package pl.btsoftware.backend.users.application;

public class InviteToGroupCommand {
    private final String inviteeEmail;

    public InviteToGroupCommand(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }
}