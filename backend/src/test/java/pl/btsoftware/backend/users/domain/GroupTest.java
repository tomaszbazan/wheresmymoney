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

        assertThat(group.getId()).isNotNull();
        assertThat(group.getName()).isEqualTo(name);
        assertThat(group.getDescription()).isEqualTo(description);
        assertThat(group.getCreatedBy()).isEqualTo(creatorId);
        assertThat(group.hasMember(creatorId)).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(1);
        assertThat(group.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateGroupWithNullDescription() {
        String name = "Test Group";
        UserId creatorId = UserId.generate();

        Group group = Group.create(name, null, creatorId);

        assertThat(group.getDescription()).isEmpty();
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

        assertThat(group.getName()).isEqualTo("Test Group");
        assertThat(group.getDescription()).isEqualTo("Test Description");
    }

    @Test
    void shouldAddMember() {
        Group group = Group.create("Test Group", "Description", UserId.generate());
        UserId newMember = UserId.generate();

        group.addMember(newMember);

        assertThat(group.hasMember(newMember)).isTrue();
        assertThat(group.getMemberCount()).isEqualTo(2);
    }

    @Test
    void shouldRemoveMember() {
        UserId creator = UserId.generate();
        UserId member = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);
        group.addMember(member);

        group.removeMember(member);

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
    void shouldUpdateName() {
        Group group = Group.create("Old Name", "Description", UserId.generate());
        String newName = "New Name";

        group.updateName(newName);

        assertThat(group.getName()).isEqualTo(newName);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingToEmptyName() {
        Group group = Group.create("Old Name", "Description", UserId.generate());

        assertThatThrownBy(() -> group.updateName(""))
                .isInstanceOf(GroupNameEmptyException.class);
    }

    @Test
    void shouldUpdateDescription() {
        Group group = Group.create("Name", "Old Description", UserId.generate());
        String newDescription = "New Description";

        group.updateDescription(newDescription);

        assertThat(group.getDescription()).isEqualTo(newDescription);
    }

    @Test
    void shouldUpdateDescriptionToEmptyWhenNull() {
        Group group = Group.create("Name", "Old Description", UserId.generate());

        group.updateDescription(null);

        assertThat(group.getDescription()).isEmpty();
    }

    @Test
    void shouldNotBeEmpty() {
        Group group = Group.create("Test Group", "Description", UserId.generate());

        assertThat(group.isEmpty()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCreatedWithEmptyMembers() {
        assertThatThrownBy(() -> new Group(GroupId.generate(), "Name", "Description", Set.of(), UserId.generate(), Instant.now()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldBeEqualWhenSameId() {
        GroupId groupId = GroupId.generate();
        UserId creatorId = UserId.generate();
        Instant now = Instant.now();

        Group group1 = new Group(groupId, "Name1", "Desc1", Set.of(creatorId), creatorId, now);
        Group group2 = new Group(groupId, "Name2", "Desc2", Set.of(UserId.generate()), UserId.generate(), now);

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