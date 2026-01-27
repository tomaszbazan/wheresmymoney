package pl.btsoftware.backend.users.infrastructure.api;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import pl.btsoftware.backend.users.domain.Group;

public record GroupView(
        String id,
        String name,
        String description,
        List<String> memberIds,
        String createdBy,
        Instant createdAt,
        int memberCount) {
    public GroupView(
            String id,
            String name,
            String description,
            List<String> memberIds,
            String createdBy,
            Instant createdAt,
            int memberCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.memberIds = memberIds != null ? List.copyOf(memberIds) : List.of();
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.memberCount = memberCount;
    }

    public static GroupView from(Group group) {
        return new GroupView(
                group.id().toString(),
                group.name(),
                group.description(),
                group.memberIds().stream().map(Object::toString).collect(Collectors.toList()),
                group.createdBy().toString(),
                group.createdAt(),
                group.getMemberCount());
    }
}
