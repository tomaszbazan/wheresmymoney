package pl.btsoftware.backend.users.domain;

import java.util.Objects;
import java.util.UUID;

public class GroupId {
    private final UUID value;

    public GroupId(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("GroupId cannot be null");
        }
        this.value = value;
    }

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }

    public static GroupId of(String value) {
        return new GroupId(UUID.fromString(value));
    }

    public UUID getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupId groupId = (GroupId) o;
        return Objects.equals(value, groupId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}