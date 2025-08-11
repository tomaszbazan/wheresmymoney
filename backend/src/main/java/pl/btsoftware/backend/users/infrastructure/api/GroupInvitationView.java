package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.GroupInvitation;
import pl.btsoftware.backend.users.domain.InvitationStatus;

import java.time.Instant;

public class GroupInvitationView {
    private final String id;
    private final String groupId;
    private final String inviteeEmail;
    private final String invitationToken;
    private final String invitedBy;
    private final InvitationStatus status;
    private final Instant createdAt;
    private final Instant expiresAt;
    private final boolean expired;
    private final boolean pending;

    public GroupInvitationView(String id, String groupId, String inviteeEmail, String invitationToken,
                              String invitedBy, InvitationStatus status, Instant createdAt, Instant expiresAt,
                              boolean expired, boolean pending) {
        this.id = id;
        this.groupId = groupId;
        this.inviteeEmail = inviteeEmail;
        this.invitationToken = invitationToken;
        this.invitedBy = invitedBy;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.expired = expired;
        this.pending = pending;
    }

    public static GroupInvitationView from(GroupInvitation invitation) {
        return new GroupInvitationView(
            invitation.getId().toString(),
            invitation.getGroupId().toString(),
            invitation.getInviteeEmail(),
            invitation.getInvitationToken(),
            invitation.getInvitedBy().toString(),
            invitation.getStatus(),
            invitation.getCreatedAt(),
            invitation.getExpiresAt(),
            invitation.isExpired(),
            invitation.isPending()
        );
    }

    public String getId() {
        return id;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public String getInvitedBy() {
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

    public boolean isExpired() {
        return expired;
    }

    public boolean isPending() {
        return pending;
    }
}