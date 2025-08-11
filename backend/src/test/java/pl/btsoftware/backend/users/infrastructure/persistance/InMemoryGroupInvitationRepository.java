package pl.btsoftware.backend.users.infrastructure.persistance;

import pl.btsoftware.backend.users.domain.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InMemoryGroupInvitationRepository implements GroupInvitationRepository {
    private final Map<GroupInvitationId, GroupInvitation> invitations = new HashMap<>();

    @Override
    public GroupInvitation save(GroupInvitation invitation) {
        invitations.put(invitation.getId(), invitation);
        return invitation;
    }

    @Override
    public Optional<GroupInvitation> findById(GroupInvitationId invitationId) {
        return Optional.ofNullable(invitations.get(invitationId));
    }

    @Override
    public Optional<GroupInvitation> findByToken(String token) {
        return invitations.values().stream()
            .filter(invitation -> invitation.getInvitationToken().equals(token))
            .findFirst();
    }

    @Override
    public List<GroupInvitation> findPendingByGroupId(GroupId groupId) {
        return invitations.values().stream()
            .filter(invitation -> invitation.getGroupId().equals(groupId))
            .filter(GroupInvitation::isPending)
            .collect(Collectors.toList());
    }

    @Override
    public List<GroupInvitation> findPendingByEmail(String email) {
        return invitations.values().stream()
            .filter(invitation -> invitation.getInviteeEmail().equals(email))
            .filter(GroupInvitation::isPending)
            .collect(Collectors.toList());
    }

    @Override
    public void deleteById(GroupInvitationId invitationId) {
        invitations.remove(invitationId);
    }

    @Override
    public void deleteExpired() {
        List<GroupInvitationId> expiredIds = invitations.values().stream()
            .filter(GroupInvitation::isExpired)
            .map(GroupInvitation::getId)
            .collect(Collectors.toList());
        
        expiredIds.forEach(invitations::remove);
    }

    public void clear() {
        invitations.clear();
    }

    public int size() {
        return invitations.size();
    }
}