package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.GroupNameEmptyException;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupTest {

    @Test
    void shouldCreateGroupWithValidData() {
        String name = "Test Group";
        String description = "Test Description";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, description, creatorId);

        assertThat(group.id()).isNotNull();
        assertThat(group.name()).isEqualTo(name);
        assertThat(group.description()).isEqualTo(description);
        assertThat(group.createdBy()).isEqualTo(creatorId);
        assertThat(group.hasMember(creatorId)).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(1);
        assertThat(group.createdAt()).isNotNull();
    }

    @Test
    void shouldCreateGroupWithNullDescription() {
        String name = "Test Group";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, null, creatorId);

        assertThat(group.description()).isEmpty();
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        assertThatThrownBy(() -> Group.create(null, "Description", UserId.generate()))
                .isInstanceOf(GroupNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsEmpty() {
        assertThatThrownBy(() -> Group.create("", "Description", UserId.generate()))
                .isInstanceOf(GroupNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        assertThatThrownBy(() -> Group.create("   ", "Description", UserId.generate()))
                .isInstanceOf(GroupNameEmptyException.class);
    }

    @Test
    void shouldTrimNameAndDescription() {
        String name = "  Test Group  ";
        String description = "  Test Description  ";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, description, creatorId);

        assertThat(group.name()).isEqualTo("Test Group");
        assertThat(group.description()).isEqualTo("Test Description");
    }

    @Test
    void shouldAddMember() {
        Group group = Group.create("Test Group", "Description", UserId.generate());
        UserId newMember = UserId.generate();

        group = group.addMember(newMember);

        assertThat(group.hasMember(newMember)).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(2);
    }

    @Test
    void shouldRemoveMember() {
        UserId creator = UserId.generate();
        UserId member = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);
        group = group.addMember(member);

        group = group.removeMember(member);

        assertThat(group.hasMember(member)).isFalse();
        assertThat(group.getMemberCount()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenRemovingLastMember() {
        UserId creator = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);

        assertThatThrownBy(() -> group.removeMember(creator))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotBeEmpty() {
        Group group = Group.create("Test Group", "Description", UserId.generate());

        assertThat(group.isEmpty()).isFalse();
    }

    @Test
    void shouldAllowCreationWithEmptyMembers() {
        // The canonical constructor allows empty members - only the private constructor with validation doesn't
        Group group = new Group(GroupId.generate(), "Name", "Description", Set.of(), UserId.generate(), Instant.now());

        assertThat(group.isEmpty()).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(0);
    }

    @Test
    void shouldBeEqualWhenAllFieldsAreEqual() {
        GroupId groupId = GroupId.generate();
        UserId creatorId = UserId.generate();
        Set<UserId> memberIds = Set.of(creatorId);
        Instant now = Instant.now();

        Group group1 = new Group(groupId, "Name", "Description", memberIds, creatorId, now);
        Group group2 = new Group(groupId, "Name", "Description", memberIds, creatorId, now);

        assertThat(group1).isEqualTo(group2);
        assertThat(group1.hashCode()).isEqualTo(group2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        UserId creatorId = UserId.generate();
        Instant now = Instant.now();

        Group group1 = new Group(GroupId.generate(), "Name", "Desc", Set.of(creatorId), creatorId, now);
        Group group2 = new Group(GroupId.generate(), "Name", "Desc", Set.of(creatorId), creatorId, now);

        assertThat(group1).isNotEqualTo(group2);
    }
}