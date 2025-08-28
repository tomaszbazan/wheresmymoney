package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        ExternalAuthId externalAuthId = new ExternalAuthId("ext-auth-123");
        String email = "test@example.com";
        String displayName = "John Doe";
        GroupId groupId = GroupId.generate();

        User user = User.create(externalAuthId, email, displayName, groupId);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getExternalAuthId()).isEqualTo(externalAuthId);
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getGroupId()).isEqualTo(groupId);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getLastLoginAt()).isNotNull();
        assertThat(user.getJoinedGroupAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), null, "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), "", "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), "   ", "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsNull() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), "test@example.com", null, GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsEmpty() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), "test@example.com", "", GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsBlank() {
        assertThatThrownBy(() -> User.create(new ExternalAuthId("ext-auth-123"), "test@example.com", "   ", GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldTrimEmailAndDisplayName() {
        String email = "  test@example.com  ";
        String displayName = "  John Doe  ";

        User user = User.create(new ExternalAuthId("ext-auth-123"), email, displayName, GroupId.generate());

        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getDisplayName()).isEqualTo("John Doe");
    }

    @Test
    void shouldChangeGroup() {
        User user = User.create(new ExternalAuthId("ext-auth-123"), "test@example.com", "John Doe", GroupId.generate());
        GroupId newGroupId = GroupId.generate();
        Instant beforeChange = Instant.now();

        user.changeGroup(newGroupId);

        assertThat(user.getGroupId()).isEqualTo(newGroupId);
        assertThat(user.getJoinedGroupAt()).isAfterOrEqualTo(beforeChange);
    }

    @Test
    void shouldThrowExceptionWhenChangingToNullGroup() {
        User user = User.create(new ExternalAuthId("ext-auth-123"), "test@example.com", "John Doe", GroupId.generate());

        assertThatThrownBy(() -> user.changeGroup(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldBeEqualWhenSameId() {
        UserId userId = UserId.generate();
        GroupId groupId = GroupId.generate();
        Instant now = Instant.now();

        User user1 = new User(userId, new ExternalAuthId("ext-auth-123"), "test@example.com", "John Doe", groupId, now, now, now);
        User user2 = new User(userId, new ExternalAuthId("ext-auth-456"), "other@example.com", "Jane Doe", GroupId.generate(), now, now, now);

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        GroupId groupId = GroupId.generate();
        Instant now = Instant.now();

        User user1 = new User(UserId.generate(), new ExternalAuthId("ext-auth-123"), "test@example.com", "John Doe", groupId, now, now, now);
        User user2 = new User(UserId.generate(), new ExternalAuthId("ext-auth-123"), "test@example.com", "John Doe", groupId, now, now, now);

        assertThat(user1).isNotEqualTo(user2);
    }
}