package pl.btsoftware.backend.users.domain;

import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupInvitationTest {

    @Test
    void shouldCreateInvitationWithValidData() {
        GroupId groupId = GroupId.generate();
        String email = "test@example.com";
        UserId invitedBy = UserId.generate();

        GroupInvitation invitation = GroupInvitation.create(groupId, email, invitedBy);

        assertThat(invitation.getId()).isNotNull();
        assertThat(invitation.getGroupId()).isEqualTo(groupId);
        assertThat(invitation.getInviteeEmail()).isEqualTo(email.toLowerCase());
        assertThat(invitation.getInvitedBy()).isEqualTo(invitedBy);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(invitation.getInvitationToken()).isNotNull();
        assertThat(invitation.getInvitationToken()).hasSize(32);
        assertThat(invitation.getCreatedAt()).isNotNull();
        assertThat(invitation.getExpiresAt()).isNotNull();
        assertThat(invitation.getExpiresAt()).isAfter(invitation.getCreatedAt());
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

        assertThat(invitation.getInviteeEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldAcceptPendingInvitation() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        invitation.accept();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
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

        assertThatThrownBy(() -> expiredInvitation.accept())
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
            InvitationStatus.ACCEPTED, now, now.plus(Duration.ofDays(7))
        );

        assertThatThrownBy(() -> acceptedInvitation.accept())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldExpireInvitation() {
        GroupInvitation invitation = GroupInvitation.create(GroupId.generate(), "test@example.com", UserId.generate());

        invitation.expire();

        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.EXPIRED);
        assertThat(invitation.isExpired()).isTrue();
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

        assertThat(invitation1.getInvitationToken()).isNotEqualTo(invitation2.getInvitationToken());
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

        assertThat(invitation1).isEqualTo(invitation2);
        assertThat(invitation1.hashCode()).isEqualTo(invitation2.hashCode());
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

        assertThat(invitation1).isNotEqualTo(invitation2);
    }
}