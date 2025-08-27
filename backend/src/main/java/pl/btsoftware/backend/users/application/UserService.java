package pl.btsoftware.backend.users.application;

import lombok.RequiredArgsConstructor;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupInvitationRepository invitationRepository;

    public User registerUser(RegisterUserCommand command) {
        if (userRepository.existsByExternalAuthId(command.externalAuthId())) {
            throw new IllegalStateException("User with external auth ID already exists");
        }

        var user = User.create(
                command.externalAuthId(),
                command.email(),
                command.displayName(),
                GroupId.generate()
        );
        
        GroupId groupId;

        if (command.hasInvitationToken()) {
            groupId = handleInvitationBasedRegistration(command);
            user.changeGroup(groupId);
        } else {
            groupId = createNewGroupForUser(command, user.getId());
            user.changeGroup(groupId);
        }

        userRepository.save(user);
        
        if (command.hasInvitationToken()) {
            Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));
            group.addMember(user.getId());
            groupRepository.save(group);
        }

        return user;
    }

    public Optional<User> findByExternalAuthId(String externalAuthId) {
        return userRepository.findByExternalAuthId(externalAuthId);
    }

    public Optional<User> findById(UserId userId) {
        return userRepository.findById(userId);
    }

    public List<User> findGroupMembers(GroupId groupId) {
        return userRepository.findByGroupId(groupId);
    }

    public void transferUserToGroup(UserId userId, GroupId newGroupId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        GroupId oldGroupId = user.getGroupId();
        
        user.changeGroup(newGroupId);
        userRepository.save(user);
        
        Group newGroup = groupRepository.findById(newGroupId)
            .orElseThrow(() -> new IllegalArgumentException("Target group not found"));
        newGroup.addMember(userId);
        groupRepository.save(newGroup);
        
        cleanupOldGroupIfEmpty(oldGroupId, userId);
    }

    private GroupId handleInvitationBasedRegistration(RegisterUserCommand command) {
        GroupInvitation invitation = invitationRepository.findByToken(command.invitationToken())
            .orElseThrow(InvitationNotFoundException::new);
        
        invitation.accept();
        invitationRepository.save(invitation);
        
        return invitation.getGroupId();
    }

    private GroupId createNewGroupForUser(RegisterUserCommand command, UserId userId) {
        String groupName = command.groupName() != null && !command.groupName().trim().isEmpty()
                ? command.groupName()
                : command.displayName() + "'s Group";

        var group = Group.create(groupName, "", userId);
        groupRepository.save(group);
        
        return group.getId();
    }

    private void cleanupOldGroupIfEmpty(GroupId oldGroupId, UserId removedUserId) {
        Group oldGroup = groupRepository.findById(oldGroupId).orElse(null);
        if (oldGroup != null) {
            if (oldGroup.getMemberCount() == 1) {
                groupRepository.deleteById(oldGroupId);
            } else {
                oldGroup.removeMember(removedUserId);
                groupRepository.save(oldGroup);
            }
        }
    }
}