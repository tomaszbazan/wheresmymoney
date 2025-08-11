package pl.btsoftware.backend.users.domain;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository {
    GroupInvitation save(GroupInvitation invitation);
    Optional<GroupInvitation> findById(GroupInvitationId invitationId);
    Optional<GroupInvitation> findByToken(String token);
    List<GroupInvitation> findPendingByGroupId(GroupId groupId);
    List<GroupInvitation> findPendingByEmail(String email);
    void deleteById(GroupInvitationId invitationId);
    void deleteExpired();
}