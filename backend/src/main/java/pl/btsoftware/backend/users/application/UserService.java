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
        if (userRepository.existsByExternalAuthId(new ExternalAuthId(command.externalAuthId()))) {
            throw new IllegalStateException("User with external auth ID already exists");
        }

        if (command.hasInvitationToken()) {
            return registerUserToExistingGroup(command);
        } else {
            return registerUserInNewGroup(command);
        }
    }

    public Optional<User> findByExternalAuthId(ExternalAuthId externalAuthId) {
        return userRepository.findByExternalAuthId(externalAuthId);
    }

    public Optional<User> findById(UserId userId) {
        return userRepository.findById(userId);
    }

    public List<User> findGroupMembers(GroupId groupId) {
        return userRepository.findByGroupId(groupId);
    }

    private User registerUserToExistingGroup(RegisterUserCommand command) {
        var groupId = handleInvitationBasedRegistration(command);

        var user = User.create(
                new ExternalAuthId(command.externalAuthId()),
                command.email(),
                command.displayName(),
                groupId
        );

        userRepository.save(user);

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalStateException("Group not found"));
        var updatedGroup = group.addMember(user.getId());
        groupRepository.save(updatedGroup);

        return user;
    }

    private User registerUserInNewGroup(RegisterUserCommand command) {
        String groupName = command.groupName() != null && !command.groupName().trim().isEmpty()
                ? command.groupName()
                : command.displayName() + "'s Group";

        var user = User.create(
                new ExternalAuthId(command.externalAuthId()),
                command.email(),
                command.displayName(),
                GroupId.generate()
        );

        var group = Group.createEmpty(groupName, "", user.getId());
        user.changeGroup(group.id());

        groupRepository.save(group);
        userRepository.save(user);

        var updatedGroup = group.addMember(user.getId());
        groupRepository.save(updatedGroup);

        return user;
    }

    private GroupId handleInvitationBasedRegistration(RegisterUserCommand command) {
        var invitation = invitationRepository.findByToken(command.invitationToken())
            .orElseThrow(InvitationNotFoundException::new);
        
        invitation.accept();
        invitationRepository.save(invitation);
        
        return invitation.getGroupId();
    }
}