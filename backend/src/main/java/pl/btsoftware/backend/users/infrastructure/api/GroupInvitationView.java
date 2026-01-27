package pl.btsoftware.backend.users.infrastructure.api;

import java.time.Instant;
import pl.btsoftware.backend.users.domain.GroupInvitation;
import pl.btsoftware.backend.users.domain.InvitationStatus;

public record GroupInvitationView(
        String id,
        String groupId,
        String inviteeEmail,
        String invitationToken,
        String invitedBy,
        InvitationStatus status,
        Instant createdAt,
        Instant expiresAt,
        boolean expired,
        boolean pending) {

    public static GroupInvitationView from(GroupInvitation invitation) {
        return new GroupInvitationView(
                invitation.id().toString(),
                invitation.groupId().toString(),
                invitation.inviteeEmail(),
                invitation.invitationToken(),
                invitation.invitedBy().toString(),
                invitation.status(),
                invitation.createdAt(),
                invitation.expiresAt(),
                invitation.isExpired(),
                invitation.isPending());
    }
}
