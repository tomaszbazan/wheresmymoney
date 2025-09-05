package pl.btsoftware.backend.users.infrastructure.api;

import pl.btsoftware.backend.users.domain.GroupInvitation;
import pl.btsoftware.backend.users.domain.InvitationStatus;

import java.time.Instant;

public record GroupInvitationView(String id, String groupId, String inviteeEmail, String invitationToken,
                                  String invitedBy, InvitationStatus status, Instant createdAt, Instant expiresAt,
                                  boolean expired, boolean pending) {

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
}