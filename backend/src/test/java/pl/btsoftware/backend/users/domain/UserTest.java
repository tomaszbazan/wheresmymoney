package pl.btsoftware.backend.users.domain;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        // given
        var UserId = new UserId("ext-auth-123");
        var email = "test@example.com";
        var displayName = "John Doe";
        var groupId = GroupId.generate();

        // when
        var user = User.create(UserId, email, displayName, groupId);

        // then
        assertThat(user.id()).isNotNull();
        assertThat(user.email()).isEqualTo(email);
        assertThat(user.displayName()).isEqualTo(displayName);
        assertThat(user.groupId()).isEqualTo(groupId);
        assertThat(user.createdAt()).isNotNull();
        assertThat(user.lastLoginAt()).isNotNull();
        assertThat(user.joinedGroupAt()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        // given
        var user = Instancio.of(User.class).setBlank(field(User::email)).create();

        // except
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), null, "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), "", "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), "   ", "John Doe", GroupId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsNull() {
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), "test@example.com", null, GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsEmpty() {
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), "test@example.com", "", GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsBlank() {
        assertThatThrownBy(() -> User.create(new UserId("ext-auth-123"), "test@example.com", "   ", GroupId.generate()))
                .isInstanceOf(DisplayNameEmptyException.class);
    }

    @Test
    void shouldTrimEmailAndDisplayName() {
        String email = "  test@example.com  ";
        String displayName = "  John Doe  ";

        User user = User.create(new UserId("ext-auth-123"), email, displayName, GroupId.generate());

        assertThat(user.email()).isEqualTo("test@example.com");
        assertThat(user.displayName()).isEqualTo("John Doe");
    }

    @Test
    void shouldChangeGroup() {
        // given
        var user = User.create(new UserId("ext-auth-123"), "test@example.com", "John Doe", GroupId.generate());
        var newGroupId = GroupId.generate();
        var beforeChange = Instant.now();

        // when
        var changedUser = user.changeGroup(newGroupId);

        // then
        assertThat(changedUser.groupId()).isEqualTo(newGroupId);
        assertThat(changedUser.joinedGroupAt()).isAfterOrEqualTo(beforeChange);
    }

    @Test
    void shouldThrowExceptionWhenChangingToNullGroup() {
        User user = User.create(new UserId("ext-auth-123"), "test@example.com", "John Doe", GroupId.generate());

        assertThatThrownBy(() -> user.changeGroup(null))
                .isInstanceOf(NullPointerException.class);
    }
}