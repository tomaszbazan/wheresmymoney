package pl.btsoftware.backend.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.btsoftware.backend.users.application.GroupService;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.application.UserService;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.UserNotFoundException;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsersModuleFacade {
    private final UserService userService;
    private final GroupService groupService;

    public User registerUser(RegisterUserCommand command) {
        return userService.registerUser(command);
    }

    public GroupInvitation inviteToGroup(UserId inviterId, InviteToGroupCommand command) {
        return groupService.inviteToGroup(inviterId, command);
    }

    public Optional<GroupInvitation> findInvitationByToken(String token) {
        return groupService.findInvitationByToken(token);
    }

    public void acceptInvitation(String token, UserId userId) {
        groupService.acceptInvitation(token, userId);
    }

    public Optional<Group> findGroupById(GroupId groupId) {
        return groupService.findGroupById(groupId);
    }

    public User findUserOrThrow(UserId userId) {
        return userService.findById(userId).orElseThrow(UserNotFoundException::new);
    }
}