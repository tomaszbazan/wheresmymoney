package pl.btsoftware.backend.users.infrastructure.persistance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.btsoftware.backend.users.domain.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "group_invitations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private String invitedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private InvitationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public static GroupInvitationEntity from(GroupInvitation invitation) {
        return new GroupInvitationEntity(
            invitation.id().value(),
                invitation.groupId().value(),
            invitation.inviteeEmail(),
            invitation.invitationToken(),
                invitation.invitedBy().value(),
            invitation.status(),
            invitation.createdAt(),
            invitation.expiresAt()
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
}
