package pl.btsoftware.backend.users.domain;

import pl.btsoftware.backend.users.domain.error.InvitationTokenExpiredException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class GroupInvitation {
    private static final Duration DEFAULT_VALIDITY = Duration.ofDays(7);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int TOKEN_LENGTH = 32;

    private final GroupInvitationId id;
    private final GroupId groupId;
    private final String inviteeEmail;
    private final String invitationToken;
    private final UserId invitedBy;
    private InvitationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;

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

    public void accept() {
        validateNotExpired();
        if (status != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation is not in pending status");
        }
        this.status = InvitationStatus.ACCEPTED;
    }

    public void expire() {
        this.status = InvitationStatus.EXPIRED;
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

    public GroupInvitationId getId() {
        return id;
    }

    public GroupId getGroupId() {
        return groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public UserId getInvitedBy() {
        return invitedBy;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupInvitation that = (GroupInvitation) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}