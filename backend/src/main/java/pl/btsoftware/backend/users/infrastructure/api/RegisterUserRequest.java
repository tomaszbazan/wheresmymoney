package pl.btsoftware.backend.users.infrastructure.api;

public class RegisterUserRequest {
    private String externalAuthId;
    private String email;
    private String displayName;
    private String groupName;

    public RegisterUserRequest() {}

    public RegisterUserRequest(String externalAuthId, String email, String displayName, String groupName) {
        this.externalAuthId = externalAuthId;
        this.email = email;
        this.displayName = displayName;
        this.groupName = groupName;
    }

    public String getExternalAuthId() {
        return externalAuthId;
    }

    public void setExternalAuthId(String externalAuthId) {
        this.externalAuthId = externalAuthId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}