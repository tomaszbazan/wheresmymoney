package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
import pl.btsoftware.backend.users.domain.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_invitations")
public class GroupInvitationEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "invitee_email", nullable = false)
    private String inviteeEmail;

    @Column(name = "invitation_token", nullable = false, unique = true)
    private String invitationToken;

    @Column(name = "invited_by", nullable = false)
    private UUID invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public GroupInvitationEntity() {}

    public GroupInvitationEntity(UUID id, UUID groupId, String inviteeEmail, String invitationToken,
                               UUID invitedBy, InvitationStatus status, Instant createdAt, Instant expiresAt) {
        this.id = id;
        this.groupId = groupId;
        this.inviteeEmail = inviteeEmail;
        this.invitationToken = invitationToken;
        this.invitedBy = invitedBy;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static GroupInvitationEntity from(GroupInvitation invitation) {
        return new GroupInvitationEntity(
            invitation.getId().getValue(),
            invitation.getGroupId().getValue(),
            invitation.getInviteeEmail(),
            invitation.getInvitationToken(),
            invitation.getInvitedBy().getValue(),
            invitation.getStatus(),
            invitation.getCreatedAt(),
            invitation.getExpiresAt()
        );
    }

    public GroupInvitation toDomain() {
        return new GroupInvitation(
            new GroupInvitationId(id),
            new GroupId(groupId),
            inviteeEmail,
            invitationToken,
            new UserId(invitedBy),
            status,
            createdAt,
            expiresAt
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getInviteeEmail() {
        return inviteeEmail;
    }

    public void setInviteeEmail(String inviteeEmail) {
        this.inviteeEmail = inviteeEmail;
    }

    public String getInvitationToken() {
        return invitationToken;
    }

    public void setInvitationToken(String invitationToken) {
        this.invitationToken = invitationToken;
    }

    public UUID getInvitedBy() {
        return invitedBy;
    }

    public void setInvitedBy(UUID invitedBy) {
        this.invitedBy = invitedBy;
    }

    public InvitationStatus getStatus() {
        return status;
    }

    public void setStatus(InvitationStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}