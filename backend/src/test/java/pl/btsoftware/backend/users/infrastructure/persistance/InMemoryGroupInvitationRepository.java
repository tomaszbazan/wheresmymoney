package pl.btsoftware.backend.users.infrastructure.persistance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.GroupInvitation;
import pl.btsoftware.backend.users.domain.GroupInvitationId;
import pl.btsoftware.backend.users.domain.GroupInvitationRepository;

public class InMemoryGroupInvitationRepository implements GroupInvitationRepository {
    private final Map<GroupInvitationId, GroupInvitation> invitations = new HashMap<>();

    @Override
    public GroupInvitation save(GroupInvitation invitation) {
        invitations.put(invitation.id(), invitation);
        return invitation;
    }

    @Override
    public Optional<GroupInvitation> findById(GroupInvitationId invitationId) {
        return Optional.ofNullable(invitations.get(invitationId));
    }

    @Override
    public Optional<GroupInvitation> findByToken(String token) {
        return invitations.values().stream()
                .filter(invitation -> invitation.invitationToken().equals(token))
                .findFirst();
    }

    @Override
    public List<GroupInvitation> findPendingByGroupId(GroupId groupId) {
        return invitations.values().stream()
                .filter(invitation -> invitation.groupId().equals(groupId))
                .filter(GroupInvitation::isPending)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupInvitation> findPendingByEmail(String email) {
        return invitations.values().stream()
                .filter(invitation -> invitation.inviteeEmail().equals(email))
                .filter(GroupInvitation::isPending)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(GroupInvitationId invitationId) {
        invitations.remove(invitationId);
    }

    @Override
    public void deleteExpired() {
        List<GroupInvitationId> expiredIds =
                invitations.values().stream()
                        .filter(GroupInvitation::isExpired)
                        .map(GroupInvitation::id)
                        .toList();

        expiredIds.forEach(invitations::remove);
    }

    public int size() {
        return invitations.size();
    }
}
