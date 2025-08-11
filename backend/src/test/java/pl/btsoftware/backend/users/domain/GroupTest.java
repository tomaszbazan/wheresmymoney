package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.GroupNameEmptyException;

import java.time.Instant;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GroupTest {

    @Test
    void shouldCreateGroupWithValidData() {
        String name = "Test Group";
        String description = "Test Description";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, description, creatorId);

        assertNotNull(group.getId());
        assertEquals(name, group.getName());
        assertEquals(description, group.getDescription());
        assertEquals(creatorId, group.getCreatedBy());
        assertTrue(group.hasMember(creatorId));
        assertEquals(1, group.getMemberCount());
        assertNotNull(group.getCreatedAt());
    }

    @Test
    void shouldCreateGroupWithNullDescription() {
        String name = "Test Group";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, null, creatorId);

        assertEquals("", group.getDescription());
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThrows(GroupNameEmptyException.class, () -> {
            Group.create(null, "Description", UserId.generate());
        });
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThrows(GroupNameEmptyException.class, () -> {
            Group.create("", "Description", UserId.generate());
        });
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThrows(GroupNameEmptyException.class, () -> {
            Group.create("   ", "Description", UserId.generate());
        });
    }

    @Test
    void shouldTrimNameAndDescription() {
        String name = "  Test Group  ";
        String description = "  Test Description  ";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, description, creatorId);

        assertEquals("Test Group", group.getName());
        assertEquals("Test Description", group.getDescription());
    }

    @Test
    void shouldAddMember() {
        Group group = Group.create("Test Group", "Description", UserId.generate());
        UserId newMember = UserId.generate();

        group.addMember(newMember);

        assertTrue(group.hasMember(newMember));
        assertEquals(2, group.getMemberCount());
    }

    @Test
    void shouldRemoveMember() {
        UserId creator = UserId.generate();
        UserId member = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);
        group.addMember(member);

        group.removeMember(member);

        assertFalse(group.hasMember(member));
        assertEquals(1, group.getMemberCount());
    }

    @Test
    void shouldThrowExceptionWhenRemovingLastMember() {
        UserId creator = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);

        assertThrows(IllegalStateException.class, () -> {
            group.removeMember(creator);
        });
    }

    @Test
    void shouldUpdateName() {
        Group group = Group.create("Old Name", "Description", UserId.generate());
        String newName = "New Name";

        group.updateName(newName);

        assertEquals(newName, group.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToEmptyName() {
        Group group = Group.create("Old Name", "Description", UserId.generate());

        assertThrows(GroupNameEmptyException.class, () -> {
            group.updateName("");
        });
    }

    @Test
    void shouldUpdateDescription() {
        Group group = Group.create("Name", "Old Description", UserId.generate());
        String newDescription = "New Description";

        group.updateDescription(newDescription);

        assertEquals(newDescription, group.getDescription());
    }

    @Test
    void shouldUpdateDescriptionToEmptyWhenNull() {
        Group group = Group.create("Name", "Old Description", UserId.generate());

        group.updateDescription(null);

        assertEquals("", group.getDescription());
    }

    @Test
    void shouldNotBeEmpty() {
        Group group = Group.create("Test Group", "Description", UserId.generate());

        assertFalse(group.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenCreatedWithEmptyMembers() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Group(GroupId.generate(), "Name", "Description", Set.of(), UserId.generate(), Instant.now());
        });
    }

    @Test
    void shouldBeEqualWhenSameId() {
        GroupId groupId = GroupId.generate();
        UserId creatorId = UserId.generate();
        Instant now = Instant.now();

        Group group1 = new Group(groupId, "Name1", "Desc1", Set.of(creatorId), creatorId, now);
        Group group2 = new Group(groupId, "Name2", "Desc2", Set.of(UserId.generate()), UserId.generate(), now);

        assertEquals(group1, group2);
        assertEquals(group1.hashCode(), group2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        UserId creatorId = UserId.generate();
        Instant now = Instant.now();

        Group group1 = new Group(GroupId.generate(), "Name", "Desc", Set.of(creatorId), creatorId, now);
        Group group2 = new Group(GroupId.generate(), "Name", "Desc", Set.of(creatorId), creatorId, now);

        assertNotEquals(group1, group2);
    }
}