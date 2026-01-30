package pl.btsoftware.backend.users.application;

public record RegisterUserCommand(
        String externalAuthId, String email, String displayName, String groupName, String invitationToken) {

    public boolean hasInvitationToken() {
        return invitationToken != null && !invitationToken.trim().isEmpty();
    }
}
