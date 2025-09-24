package pl.btsoftware.backend.users.domain;

import lombok.AccessLevel;
import lombok.With;
import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record GroupInvitation(GroupInvitationId id, GroupId groupId, String inviteeEmail, String invitationToken,
                              UserId invitedBy, @With(value = AccessLevel.PRIVATE) InvitationStatus status,
                              Instant createdAt, Instant expiresAt) {
    private static final Duration DEFAULT_VALIDITY = Duration.ofDays(7);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 32;

    public GroupInvitation(GroupInvitationId id, GroupId groupId, String inviteeEmail,
                           String invitationToken, UserId invitedBy, InvitationStatus status,
                           Instant createdAt, Instant expiresAt) {
        validateEmail(inviteeEmail);

        this.id = Objects.requireNonNull(id, "GroupInvitationId cannot be null");
        this.groupId = Objects.requireNonNull(groupId, "GroupId cannot be null");
        this.inviteeEmail = inviteeEmail.trim().toLowerCase();
        this.invitationToken = Objects.requireNonNull(invitationToken, "Invitation token cannot be null");
        this.invitedBy = Objects.requireNonNull(invitedBy, "InvitedBy cannot be null");
        this.status = Objects.requireNonNull(status, "Status cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null");
    }

    public static GroupInvitation create(GroupId groupId, String inviteeEmail, UserId invitedBy) {
        Instant now = Instant.now();
        return new GroupInvitation(
                GroupInvitationId.generate(),
                groupId,
                inviteeEmail,
                generateToken(),
                invitedBy,
                InvitationStatus.PENDING,
                now,
                now.plus(DEFAULT_VALIDITY)
        );
    }

    public GroupInvitation accept() {
        validateNotExpired();
        invitationIsPending();
        return withStatus(InvitationStatus.ACCEPTED);
    }

    public GroupInvitation expire() {
        return withStatus(InvitationStatus.EXPIRED);
    }

    private void invitationIsPending() {
        if (!isPending()) {
            throw new IllegalStateException("Invitation is not in pending status");
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt) || status == InvitationStatus.EXPIRED;
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING && !isExpired();
    }

    private void validateNotExpired() {
        if (isExpired()) {
            throw new InvitationTokenExpiredException();
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new UserEmailEmptyException();
        }
    }

    private static String generateToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(TOKEN_CHARS.charAt(RANDOM.nextInt(TOKEN_CHARS.length())));
        }
        return token.toString();
    }
}