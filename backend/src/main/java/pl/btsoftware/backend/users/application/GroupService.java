package pl.btsoftware.backend.users.application;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;

import java.util.Optional;

@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupInvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public GroupInvitation inviteToGroup(UserId inviterId, InviteToGroupCommand command) {
        var group = groupRepository.findByUserId(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("Inviter's group not found"));

        var invitation = GroupInvitation.create(
                group.id(),
                command.inviteeEmail(),
                inviterId
        );

        return invitationRepository.save(invitation);
    }

    public Optional<GroupInvitation> findInvitationByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    public void acceptInvitation(String token, UserId userId) {
        var invitation = invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        invitation.accept();
        invitationRepository.save(invitation);

        var oldGroupId = user.groupId();
        var newGroupId = invitation.getGroupId();

        var updatedUser = user.changeGroup(newGroupId);
        userRepository.save(updatedUser);

        Group newGroup = groupRepository.findById(newGroupId)
                .orElseThrow(() -> new IllegalStateException("Target group not found"));
        var updatedNewGroup = newGroup.addMember(userId);
        groupRepository.save(updatedNewGroup);

        cleanupOldGroupIfEmpty(oldGroupId, userId);
    }

    public Optional<Group> findGroupById(GroupId groupId) {
        return groupRepository.findById(groupId);
    }

    private void cleanupOldGroupIfEmpty(GroupId oldGroupId, UserId removedUserId) {
        Group oldGroup = groupRepository.findById(oldGroupId).orElse(null);
        if (oldGroup != null) {
            var updatedOldGroup = oldGroup.removeMember(removedUserId);

            if (updatedOldGroup.isEmpty()) {
                groupRepository.deleteById(oldGroupId);
            } else {
                groupRepository.save(updatedOldGroup);
            }
        }
    }
}