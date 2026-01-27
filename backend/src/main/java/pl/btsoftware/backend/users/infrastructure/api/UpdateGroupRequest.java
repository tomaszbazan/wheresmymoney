package pl.btsoftware.backend.users.infrastructure.api;

import jakarta.validation.constraints.NotBlank;

public record UpdateGroupRequest(@NotBlank String name, String description) {
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
