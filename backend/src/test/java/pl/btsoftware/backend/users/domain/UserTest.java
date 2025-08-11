package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithValidData() {
        String externalAuthId = "ext-auth-123";
        String email = "test@example.com";
        String displayName = "John Doe";
        GroupId groupId = GroupId.generate();

        User user = User.create(externalAuthId, email, displayName, groupId);

        assertNotNull(user.getId());
        assertEquals(externalAuthId, user.getExternalAuthId());
        assertEquals(email, user.getEmail());
        assertEquals(displayName, user.getDisplayName());
        assertEquals(groupId, user.getGroupId());
        assertNotNull(user.getCreatedAt());
        assertNotNull(user.getLastLoginAt());
        assertNotNull(user.getJoinedGroupAt());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(UserEmailEmptyException.class, () -> {
            User.create("ext-auth-123", null, "John Doe", GroupId.generate());
        });
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThrows(UserEmailEmptyException.class, () ->
                User.create("ext-auth-123", "", "John Doe", GroupId.generate()));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThrows(UserEmailEmptyException.class, () ->
                User.create("ext-auth-123", "   ", "John Doe", GroupId.generate()));
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsNull() {
        assertThrows(DisplayNameEmptyException.class, () ->
                User.create("ext-auth-123", "test@example.com", null, GroupId.generate()));
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsEmpty() {
        assertThrows(DisplayNameEmptyException.class, () ->
                User.create("ext-auth-123", "test@example.com", "", GroupId.generate()));
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsBlank() {
        assertThrows(DisplayNameEmptyException.class, () ->
                User.create("ext-auth-123", "test@example.com", "   ", GroupId.generate()));
    }

    @Test
    void shouldTrimEmailAndDisplayName() {
        String email = "  test@example.com  ";
        String displayName = "  John Doe  ";

        User user = User.create("ext-auth-123", email, displayName, GroupId.generate());

        assertEquals("test@example.com", user.getEmail());
        assertEquals("John Doe", user.getDisplayName());
    }

    @Test
    void shouldChangeGroup() {
        User user = User.create("ext-auth-123", "test@example.com", "John Doe", GroupId.generate());
        GroupId newGroupId = GroupId.generate();
        Instant beforeChange = Instant.now();

        user.changeGroup(newGroupId);

        assertEquals(newGroupId, user.getGroupId());
        assertTrue(user.getJoinedGroupAt().isAfter(beforeChange) || user.getJoinedGroupAt().equals(beforeChange));
    }

    @Test
    void shouldThrowExceptionWhenChangingToNullGroup() {
        User user = User.create("ext-auth-123", "test@example.com", "John Doe", GroupId.generate());

        assertThrows(NullPointerException.class, () -> {
            user.changeGroup(null);
        });
    }

    @Test
    void shouldBeEqualWhenSameId() {
        UserId userId = UserId.generate();
        GroupId groupId = GroupId.generate();
        Instant now = Instant.now();

        User user1 = new User(userId, "ext-auth-123", "test@example.com", "John Doe", groupId, now, now, now);
        User user2 = new User(userId, "ext-auth-456", "other@example.com", "Jane Doe", GroupId.generate(), now, now, now);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        GroupId groupId = GroupId.generate();
        Instant now = Instant.now();

        User user1 = new User(UserId.generate(), "ext-auth-123", "test@example.com", "John Doe", groupId, now, now, now);
        User user2 = new User(UserId.generate(), "ext-auth-123", "test@example.com", "John Doe", groupId, now, now, now);

        assertNotEquals(user1, user2);
    }
}