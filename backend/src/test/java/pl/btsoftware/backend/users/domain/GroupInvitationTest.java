package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.InvitationNotPendingException;
import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pl.btsoftware.backend.users.domain.InvitationStatus.ACCEPTED;
import static pl.btsoftware.backend.users.domain.InvitationStatus.EXPIRED;

class GroupInvitationTest {

    @Test
    void shouldCreateInvitationWithValidData() {
        GroupId groupId = GroupId.generate();
        String email = "test@example.com";
        UserId invitedBy = UserId.generate();

        GroupInvitation invitation = GroupInvitation.create(groupId, email, invitedBy);

        assertThat(invitation.id()).isNotNull();
        assertThat(invitation.groupId()).isEqualTo(groupId);
        assertThat(invitation.inviteeEmail()).isEqualTo(email.toLowerCase());
        assertThat(invitation.invitedBy()).isEqualTo(invitedBy);
        assertThat(invitation.status()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.invitationToken()).isNotNull();
        assertThat(invitation.invitationToken()).hasSize(32);
        assertThat(invitation.createdAt()).isNotNull();
        assertThat(invitation.expiresAt()).isNotNull();
        assertThat(invitation.expiresAt()).isAfter(invitation.createdAt());
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        assertThatThrownBy(() -> GroupInvitation.create(GroupId.generate(), null, UserId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        assertThatThrownBy(() -> GroupInvitation.create(GroupId.generate(), "", UserId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        assertThatThrownBy(() -> GroupInvitation.create(GroupId.generate(), "   ", UserId.generate()))
                .isInstanceOf(UserEmailEmptyException.class);
    }

    @Test
    void shouldTrimAndLowercaseEmail() {
        String email = "  TEST@EXAMPLE.COM  ";

        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), email, UserId.generate());

        assertThat(invitation.inviteeEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldAcceptPendingInvitation() {
        // given
        var invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        // when
        var acceptedInvitation = invitation.accept();

        // then
        assertThat(acceptedInvitation.status()).isEqualTo(ACCEPTED);
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

        assertThatThrownBy(expiredInvitation::accept)
                .isInstanceOf(InvitationTokenExpiredException.class);
    }

    @Test
    void shouldThrowExceptionWhenAcceptingNonPendingInvitation() {
        GroupInvitationId id = GroupInvitationId.generate();
        GroupId groupId = GroupId.generate();
        UserId invitedBy = UserId.generate();
        Instant now = Instant.now();

        GroupInvitation acceptedInvitation = new GroupInvitation(
                id, groupId, "test@example.com", "token", invitedBy,
                ACCEPTED, now, now.plus(Duration.ofDays(7))
        );

        assertThatThrownBy(acceptedInvitation::accept)
                .isInstanceOf(InvitationNotPendingException.class);
    }

    @Test
    void shouldExpireInvitation() {
        // given
        var invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        // when
        var expiredInvitation = invitation.expire();

        // then
        assertThat(expiredInvitation.status()).isEqualTo(EXPIRED);
        assertThat(expiredInvitation.isExpired()).isTrue();
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

        assertThat(expiredInvitation.isExpired()).isTrue();
        assertThat(expiredInvitation.isPending()).isFalse();
    }

    @Test
    void shouldBePendingWhenNotExpired() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        assertThat(invitation.isPending()).isTrue();
        assertThat(invitation.isExpired()).isFalse();
    }

    @Test
    void shouldGenerateUniqueTokens() {
        GroupId groupId = GroupId.generate();
        String email = "test@example.com";
        UserId invitedBy = UserId.generate();

        GroupInvitation invitation1 = GroupInvitation.create(groupId, email, invitedBy);
        GroupInvitation invitation2 = GroupInvitation.create(groupId, email, invitedBy);

        assertThat(invitation1.invitationToken()).isNotEqualTo(invitation2.invitationToken());
    }
}
