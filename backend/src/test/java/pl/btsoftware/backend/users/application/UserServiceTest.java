package pl.btsoftware.backend.users.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupInvitationRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryUserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private InMemoryUserRepository userRepository;
    private InMemoryGroupRepository groupRepository;
    private InMemoryGroupInvitationRepository invitationRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        groupRepository = new InMemoryGroupRepository();
        invitationRepository = new InMemoryGroupInvitationRepository();
        userService = new UserService(userRepository, groupRepository, invitationRepository);
    }

    @Test
    void shouldRegisterUserWithNewGroup() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );

        User user = userService.registerUser(command);

        assertNotNull(user.getId());
        assertEquals("ext-auth-123", user.getExternalAuthId());
        assertEquals("test@example.com", user.getEmail());
        assertEquals("John Doe", user.getDisplayName());
        assertNotNull(user.getGroupId());
        
        assertTrue(groupRepository.existsById(user.getGroupId()));
        Group group = groupRepository.findById(user.getGroupId()).get();
        assertEquals("My Group", group.getName());
        assertTrue(group.hasMember(user.getId()));
        assertEquals(1, groupRepository.size());
        assertEquals(1, userRepository.size());
    }

    @Test
    void shouldRegisterUserWithDefaultGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", null, null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.getGroupId()).get();
        assertEquals("John Doe's Group", group.getName());
    }

    @Test
    void shouldRegisterUserWithEmptyGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.getGroupId()).get();
        assertEquals("John Doe's Group", group.getName());
    }

    @Test
    void shouldRegisterUserWithInvitation() {
        UserId inviterId = UserId.generate();
        Group existingGroup = Group.create("Existing Group", "Description", inviterId);
        groupRepository.save(existingGroup);
        
        GroupInvitation invitation = GroupInvitation.create(existingGroup.getId(), "test@example.com", inviterId);
        invitationRepository.save(invitation);

        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "Ignored Group", invitation.getInvitationToken()
        );

        User user = userService.registerUser(command);

        assertEquals(existingGroup.getId(), user.getGroupId());
        assertTrue(groupRepository.findById(existingGroup.getId()).get().hasMember(user.getId()));
        assertEquals(InvitationStatus.ACCEPTED, invitationRepository.findByToken(invitation.getInvitationToken()).get().getStatus());
        assertEquals(1, groupRepository.size());
        assertEquals(1, userRepository.size());
    }

    @Test
    void shouldThrowExceptionWhenInvitationTokenNotFound() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", "invalid-token"
        );

        assertThrows(InvitationNotFoundException.class, () -> {
            userService.registerUser(command);
        });
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExists() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );
        userService.registerUser(command);

        RegisterUserCommand duplicateCommand = new RegisterUserCommand(
            "ext-auth-123", "other@example.com", "Jane Doe", "Other Group", null
        );

        assertThrows(IllegalStateException.class, () -> {
            userService.registerUser(duplicateCommand);
        });
    }

    @Test
    void shouldFindUserByExternalAuthId() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );
        User savedUser = userService.registerUser(command);

        User foundUser = userService.findByExternalAuthId("ext-auth-123").get();

        assertEquals(savedUser.getId(), foundUser.getId());
        assertEquals("ext-auth-123", foundUser.getExternalAuthId());
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByExternalAuthId() {
        assertTrue(userService.findByExternalAuthId("non-existing").isEmpty());
    }

    @Test
    void shouldFindGroupMembers() {
        GroupId groupId = GroupId.generate();
        UserId creator = UserId.generate();
        Group group = Group.create("Test Group", "Description", creator);
        groupRepository.save(group);

        RegisterUserCommand command1 = new RegisterUserCommand(
            "ext-auth-1", "user1@example.com", "User 1", "Test Group", null
        );
        RegisterUserCommand command2 = new RegisterUserCommand(
            "ext-auth-2", "user2@example.com", "User 2", "Test Group", null
        );
        
        User user1 = userService.registerUser(command1);
        User user2 = userService.registerUser(command2);

        List<User> members1 = userService.findGroupMembers(user1.getGroupId());
        List<User> members2 = userService.findGroupMembers(user2.getGroupId());

        assertEquals(1, members1.size());
        assertEquals(1, members2.size());
        assertTrue(members1.contains(user1));
        assertTrue(members2.contains(user2));
    }

    @Test
    void shouldTransferUserToGroup() {
        RegisterUserCommand command1 = new RegisterUserCommand(
            "ext-auth-1", "user1@example.com", "User 1", "Group 1", null
        );
        RegisterUserCommand command2 = new RegisterUserCommand(
            "ext-auth-2", "user2@example.com", "User 2", "Group 2", null
        );
        
        User user1 = userService.registerUser(command1);
        User user2 = userService.registerUser(command2);
        GroupId oldGroupId = user1.getGroupId();
        GroupId newGroupId = user2.getGroupId();
        
        assertEquals(2, groupRepository.size());

        userService.transferUserToGroup(user1.getId(), newGroupId);

        User updatedUser1 = userService.findById(user1.getId()).get();
        assertEquals(newGroupId, updatedUser1.getGroupId());
        
        Group newGroup = groupRepository.findById(newGroupId).get();
        assertTrue(newGroup.hasMember(user1.getId()));
        assertTrue(newGroup.hasMember(user2.getId()));
        assertEquals(2, newGroup.getMemberCount());
        
        assertFalse(groupRepository.existsById(oldGroupId));
        assertEquals(1, groupRepository.size());
    }

    @Test
    void shouldNotDeleteGroupWhenMultipleMembers() {
        RegisterUserCommand command1 = new RegisterUserCommand(
            "ext-auth-1", "user1@example.com", "User 1", "Group 1", null
        );
        RegisterUserCommand command2 = new RegisterUserCommand(
            "ext-auth-2", "user2@example.com", "User 2", "Group 2", null
        );
        
        User user1 = userService.registerUser(command1);
        User user2 = userService.registerUser(command2);
        GroupId targetGroupId = user2.getGroupId();
        
        userService.transferUserToGroup(user1.getId(), targetGroupId);
        
        RegisterUserCommand command3 = new RegisterUserCommand(
            "ext-auth-3", "user3@example.com", "User 3", "Group 3", null
        );
        User user3 = userService.registerUser(command3);
        
        userService.transferUserToGroup(user1.getId(), user3.getGroupId());

        assertTrue(groupRepository.existsById(targetGroupId));
        Group remainingGroup = groupRepository.findById(targetGroupId).get();
        assertTrue(remainingGroup.hasMember(user2.getId()));
        assertEquals(1, remainingGroup.getMemberCount());
    }
}