package pl.btsoftware.backend.users.application;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupInvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public GroupInvitation inviteToGroup(UserId inviterId, InviteToGroupCommand command) {
        var inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new IllegalArgumentException("Inviter not found"));

        var groupId = inviter.getGroupId();

        if (!groupRepository.existsById(groupId)) {
            throw new IllegalStateException("Inviter's group not found");
        }

        var invitation = GroupInvitation.create(
                groupId,
                command.getInviteeEmail(),
                inviterId
        );

        return invitationRepository.save(invitation);
    }

    public Optional<GroupInvitation> findInvitationByToken(String token) {
        return invitationRepository.findByToken(token);
    }

    public void acceptInvitation(String token, UserId userId) {
        GroupInvitation invitation = invitationRepository.findByToken(token)
                .orElseThrow(InvitationNotFoundException::new);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        invitation.accept();
        invitationRepository.save(invitation);

        GroupId oldGroupId = user.getGroupId();
        GroupId newGroupId = invitation.getGroupId();

        user.changeGroup(newGroupId);
        userRepository.save(user);

        Group newGroup = groupRepository.findById(newGroupId)
                .orElseThrow(() -> new IllegalStateException("Target group not found"));
        newGroup.addMember(userId);
        groupRepository.save(newGroup);

        cleanupOldGroupIfEmpty(oldGroupId, userId);
    }

    public Optional<Group> findGroupById(GroupId groupId) {
        return groupRepository.findById(groupId);
    }

    public Group updateGroup(GroupId groupId, UpdateGroupCommand command) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        if (command.getName() != null) {
            group.updateName(command.getName());
        }

        if (command.getDescription() != null) {
            group.updateDescription(command.getDescription());
        }

        return groupRepository.save(group);
    }

    public List<GroupInvitation> findPendingInvitationsForGroup(GroupId groupId) {
        return invitationRepository.findPendingByGroupId(groupId);
    }

    public void cleanupExpiredInvitations() {
        invitationRepository.deleteExpired();
    }

    private void cleanupOldGroupIfEmpty(GroupId oldGroupId, UserId removedUserId) {
        Group oldGroup = groupRepository.findById(oldGroupId).orElse(null);
        if (oldGroup != null) {
            oldGroup.removeMember(removedUserId);

            if (oldGroup.isEmpty()) {
                groupRepository.deleteById(oldGroupId);
            } else {
                groupRepository.save(oldGroup);
            }
        }
    }
}