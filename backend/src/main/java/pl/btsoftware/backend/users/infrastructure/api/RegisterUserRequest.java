package pl.btsoftware.backend.users.infrastructure.api;

public record RegisterUserRequest(String externalAuthId, String email, String displayName, String groupName) {
}
