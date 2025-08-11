package pl.btsoftware.backend.users.application;

public class RegisterUserCommand {
    private final String externalAuthId;
    private final String email;
    private final String displayName;
    private final String groupName;
    private final String invitationToken;

    public RegisterUserCommand(String externalAuthId, String email, String displayName, 
                              String groupName, String invitationToken) {
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.displayName = displayName;
        this.groupName = groupName;
        this.invitationToken = invitationToken;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public boolean hasInvitationToken() {
        return invitationToken != null && !invitationToken.trim().isEmpty();
    }
}