package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.Group;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class GroupView {
    private final String id;
    private final String name;
    private final String description;
    private final List<String> memberIds;
    private final String createdBy;
    private final Instant createdAt;
    private final int memberCount;

    public GroupView(String id, String name, String description, List<String> memberIds,
                    String createdBy, Instant createdAt, int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberIds = memberIds;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
    }

    public static GroupView from(Group group) {
        return new GroupView(
            group.getId().toString(),
            group.getName(),
            group.getDescription(),
            group.getMemberIds().stream()
                .map(Object::toString)
                .collect(Collectors.toList()),
            group.getCreatedBy().toString(),
            group.getCreatedAt(),
            group.getMemberCount()
        );
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public int getMemberCount() {
        return memberCount;
    }
}