package pl.btsoftware.backend.users.infrastructure.api;

public class InviteToGroupRequest {
    private String email;

    public InviteToGroupRequest() {}

    public InviteToGroupRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}