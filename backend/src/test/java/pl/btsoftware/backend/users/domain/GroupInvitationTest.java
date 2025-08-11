package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class GroupInvitationTest {

    @Test
    void shouldCreateInvitationWithValidData() {
        GroupId groupId = GroupId.generate();
        String email = "test@example.com";
        UserId invitedBy = UserId.generate();

        GroupInvitation invitation = GroupInvitation.create(groupId, email, invitedBy);

        assertNotNull(invitation.getId());
        assertEquals(groupId, invitation.getGroupId());
        assertEquals(email.toLowerCase(), invitation.getInviteeEmail());
        assertEquals(invitedBy, invitation.getInvitedBy());
        assertEquals(InvitationStatus.PENDING, invitation.getStatus());
        assertNotNull(invitation.getInvitationToken());
        assertEquals(32, invitation.getInvitationToken().length());
        assertNotNull(invitation.getCreatedAt());
        assertNotNull(invitation.getExpiresAt());
        assertTrue(invitation.getExpiresAt().isAfter(invitation.getCreatedAt()));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThrows(UserEmailEmptyException.class, () -> {
            GroupInvitation.create(GroupId.generate(), null, UserId.generate());
        });
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThrows(UserEmailEmptyException.class, () -> {
            GroupInvitation.create(GroupId.generate(), "", UserId.generate());
        });
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThrows(UserEmailEmptyException.class, () -> {
            GroupInvitation.create(GroupId.generate(), "   ", UserId.generate());
        });
    }

    @Test
    void shouldTrimAndLowercaseEmail() {
        String email = "  TEST@EXAMPLE.COM  ";
        
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), email, UserId.generate());

        assertEquals("test@example.com", invitation.getInviteeEmail());
    }

    @Test
    void shouldAcceptPendingInvitation() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        invitation.accept();

        assertEquals(InvitationStatus.ACCEPTED, invitation.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenAcceptingExpiredInvitation() {
        GroupInvitationId id = GroupInvitationId.generate();
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant pastTime = Instant.now().minus(Duration.ofDays(8));
        
        GroupInvitation expiredInvitation = new GroupInvitation(
            id, groupId, "test@example.com", "token", invitedBy,
            InvitationStatus.PENDING, pastTime, pastTime.plus(Duration.ofDays(7))
        );

        assertThrows(InvitationTokenExpiredException.class, () -> {
            expiredInvitation.accept();
        });
    }

    @Test
    void shouldThrowExceptionWhenAcceptingNonPendingInvitation() {
        GroupInvitationId id = GroupInvitationId.generate();
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant now = Instant.now();
        
        GroupInvitation acceptedInvitation = new GroupInvitation(
            id, groupId, "test@example.com", "token", invitedBy,
            InvitationStatus.ACCEPTED, now, now.plus(Duration.ofDays(7))
        );

        assertThrows(IllegalStateException.class, () -> {
            acceptedInvitation.accept();
        });
    }

    @Test
    void shouldExpireInvitation() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        invitation.expire();

        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
        assertTrue(invitation.isExpired());
    }

    @Test
    void shouldBeExpiredWhenPastExpirationDate() {
        GroupInvitationId id = GroupInvitationId.generate();
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant pastTime = Instant.now().minus(Duration.ofDays(8));
        
        GroupInvitation expiredInvitation = new GroupInvitation(
            id, groupId, "test@example.com", "token", invitedBy,
            InvitationStatus.PENDING, pastTime, pastTime.plus(Duration.ofDays(7))
        );

        assertTrue(expiredInvitation.isExpired());
        assertFalse(expiredInvitation.isPending());
    }

    @Test
    void shouldBePendingWhenNotExpired() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        assertTrue(invitation.isPending());
        assertFalse(invitation.isExpired());
    }

    @Test
    void shouldGenerateUniqueTokens() {
        GroupId groupId = GroupId.generate();
        String email = "test@example.com";
        UserId invitedBy = UserId.generate();

        GroupInvitation invitation1 = GroupInvitation.create(groupId, email, invitedBy);
        GroupInvitation invitation2 = GroupInvitation.create(groupId, email, invitedBy);

        assertNotEquals(invitation1.getInvitationToken(), invitation2.getInvitationToken());
    }

    @Test
    void shouldBeEqualWhenSameId() {
        GroupInvitationId invitationId = GroupInvitationId.generate();
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant now = Instant.now();

        GroupInvitation invitation1 = new GroupInvitation(
            invitationId, groupId, "test1@example.com", "token1", invitedBy,
            InvitationStatus.PENDING, now, now.plus(Duration.ofDays(7))
        );
        GroupInvitation invitation2 = new GroupInvitation(
            invitationId, GroupId.generate(), "test2@example.com", "token2", UserId.generate(),
            InvitationStatus.ACCEPTED, now, now.plus(Duration.ofDays(7))
        );

        assertEquals(invitation1, invitation2);
        assertEquals(invitation1.hashCode(), invitation2.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenDifferentId() {
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant now = Instant.now();

        GroupInvitation invitation1 = new GroupInvitation(
            GroupInvitationId.generate(), groupId, "test@example.com", "token", invitedBy,
            InvitationStatus.PENDING, now, now.plus(Duration.ofDays(7))
        );
        GroupInvitation invitation2 = new GroupInvitation(
            GroupInvitationId.generate(), groupId, "test@example.com", "token", invitedBy,
            InvitationStatus.PENDING, now, now.plus(Duration.ofDays(7))
        );

        assertNotEquals(invitation1, invitation2);
    }
}