package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.Group;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record GroupView(String id, String name, String description, List<String> memberIds, String createdBy,
                        Instant createdAt, int memberCount) {

    public static GroupView from(Group group) {
        return new GroupView(
                group.id().toString(),
                group.name(),
                group.description(),
                group.memberIds().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList()),
                group.createdBy().toString(),
                group.createdAt(),
                group.getMemberCount()
        );
    }
}