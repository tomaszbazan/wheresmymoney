package pl.btsoftware.backend.users.application;

public class UpdateGroupCommand {
    private final String name;
    private final String description;

    public UpdateGroupCommand(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}