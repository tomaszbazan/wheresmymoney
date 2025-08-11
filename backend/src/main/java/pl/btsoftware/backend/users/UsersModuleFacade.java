package pl.btsoftware.backend.users;

import org.springframework.stereotype.Component;
import pl.btsoftware.backend.users.application.GroupService;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.application.UpdateGroupCommand;
import pl.btsoftware.backend.users.application.UserService;
import pl.btsoftware.backend.users.domain.*;

import java.util.List;
import java.util.Optional;

@Component
public class UsersModuleFacade {
    private final UserService userService;
    private final GroupService groupService;

    public UsersModuleFacade(UserService userService, GroupService groupService) {
        this.userService = userService;
        this.groupService = groupService;
    }

    public User registerUser(RegisterUserCommand command) {
        return userService.registerUser(command);
    }

    public Optional<User> findUserByExternalAuthId(String externalAuthId) {
        return userService.findByExternalAuthId(externalAuthId);
    }

    public Optional<User> findUserById(UserId userId) {
        return userService.findById(userId);
    }

    public List<User> findGroupMembers(GroupId groupId) {
        return userService.findGroupMembers(groupId);
    }

    public void transferUserToGroup(UserId userId, GroupId newGroupId) {
        userService.transferUserToGroup(userId, newGroupId);
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

    public Group updateGroup(GroupId groupId, UpdateGroupCommand command) {
        return groupService.updateGroup(groupId, command);
    }

    public List<GroupInvitation> findPendingInvitationsForGroup(GroupId groupId) {
        return groupService.findPendingInvitationsForGroup(groupId);
    }

    public void cleanupExpiredInvitations() {
        groupService.cleanupExpiredInvitations();
    }
}