package pl.btsoftware.backend.users.domain;

import pl.btsoftware.backend.shared.validation.NameValidationRules;
import pl.btsoftware.backend.users.domain.error.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Group(GroupId id, String name, String description, Set<UserId> memberIds, UserId createdBy,
                    Instant createdAt) {

    public Group {
        validateName(name);

        Objects.requireNonNull(id, "GroupId cannot be null");
        Objects.requireNonNull(memberIds, "Member IDs cannot be null");
        Objects.requireNonNull(createdBy, "CreatedBy cannot be null");
        Objects.requireNonNull(createdAt, "CreatedAt cannot be null");

        name = name.trim();
        description = description != null ? description.trim() : "";
        memberIds = Set.copyOf(memberIds);
    }

    private Group(GroupId id, String name, String description, Set<UserId> memberIds, UserId createdBy, Instant createdAt, boolean validateMembers) {
        this(id, name, description, memberIds, createdBy, createdAt);

        if (validateMembers && memberIds.isEmpty()) {
            throw new GroupMustHaveAtLeastOneMemberException();
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

    public static Group createEmpty(String name, String description, UserId creatorId) {
        return new Group(
                GroupId.generate(),
                name,
                description,
                new HashSet<>(),
                creatorId,
                Instant.now(),
                false
        );
    }

    public static Group createEmptyWithId(GroupId id, String name, String description, UserId creatorId, Instant createdAt) {
        return new Group(
                id,
                name,
                description,
                new HashSet<>(),
                creatorId,
                createdAt,
                false
        );
    }

    public Set<UserId> memberIds() {
        return memberIds;
    }

    public Group addMember(UserId userId) {
        Objects.requireNonNull(userId, "UserId cannot be null");
        var newMemberIds = new HashSet<>(memberIds);
        newMemberIds.add(userId);
        return new Group(id, name, description, newMemberIds, createdBy, createdAt);
    }

    public Group removeMember(UserId userId) {
        Objects.requireNonNull(userId, "UserId cannot be null");

        if (memberIds.size() <= 1) {
            throw new CannotRemoveLastGroupMemberException();
        }

        var newMemberIds = new HashSet<>(memberIds);
        newMemberIds.remove(userId);
        return new Group(id, name, description, newMemberIds, createdBy, createdAt);
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
        NameValidationRules.validate(
                name,
                GroupNameEmptyException::new,
                GroupNameTooLongException::new,
                GroupNameInvalidCharactersException::new
        );
    }
}
