package pl.btsoftware.backend.users.domain;

import pl.btsoftware.backend.users.domain.error.GroupNameEmptyException;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Group {
    private final GroupId id;
    private final String name;
    private final String description;
    private final Set<UserId> memberIds;
    private final UserId createdBy;
    private final Instant createdAt;

    public Group(GroupId id, String name, String description, Set<UserId> memberIds, 
                 UserId createdBy, Instant createdAt) {
        validateName(name);
        
        this.id = Objects.requireNonNull(id, "GroupId cannot be null");
        this.name = name.trim();
        this.description = description != null ? description.trim() : "";
        this.memberIds = new HashSet<>(Objects.requireNonNull(memberIds, "Member IDs cannot be null"));
        this.createdBy = Objects.requireNonNull(createdBy, "CreatedBy cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        
        if (this.memberIds.isEmpty()) {
            throw new IllegalArgumentException("Group must have at least one member");
        }
    }

    public static Group create(String name, String description, UserId creatorId) {
        Set<UserId> initialMembers = Set.of(creatorId);
        return new Group(
            GroupId.generate(),
            name,
            description,
            initialMembers,
            creatorId,
            Instant.now()
        );
    }

    public void addMember(UserId userId) {
        Objects.requireNonNull(userId, "UserId cannot be null");
        memberIds.add(userId);
    }

    public void removeMember(UserId userId) {
        Objects.requireNonNull(userId, "UserId cannot be null");
        
        if (memberIds.size() <= 1) {
            throw new IllegalStateException("Cannot remove last member from group");
        }
        
        memberIds.remove(userId);
    }

    public boolean hasMember(UserId userId) {
        return memberIds.contains(userId);
    }

    public boolean isEmpty() {
        return memberIds.isEmpty();
    }

    public int getMemberCount() {
        return memberIds.size();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new GroupNameEmptyException();
        }
    }

    public GroupId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Set<UserId> getMemberIds() {
        return Collections.unmodifiableSet(memberIds);
    }

    public UserId getCreatedBy() {
        return createdBy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return Objects.equals(id, group.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}