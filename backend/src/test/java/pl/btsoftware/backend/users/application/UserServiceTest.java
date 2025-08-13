package pl.btsoftware.backend.users.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.btsoftware.backend.users.domain.*;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupInvitationRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryGroupRepository;
import pl.btsoftware.backend.users.infrastructure.persistance.InMemoryUserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        assertThat(user.getId()).isNotNull();
        assertThat(user.getExternalAuthId()).isEqualTo("ext-auth-123");
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getDisplayName()).isEqualTo("John Doe");
        assertThat(user.getGroupId()).isNotNull();

        assertThat(groupRepository.existsById(user.getGroupId())).isTrue();
        Group group = groupRepository.findById(user.getGroupId()).get();
        assertThat(group.getName()).isEqualTo("My Group");
        assertThat(group.hasMember(user.getId())).isTrue();
        assertThat(groupRepository.size()).isEqualTo(1);
        assertThat(userRepository.size()).isEqualTo(1);
    }

    @Test
    void shouldRegisterUserWithDefaultGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", null, null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.getGroupId()).get();
        assertThat(group.getName()).isEqualTo("John Doe's Group");
    }

    @Test
    void shouldRegisterUserWithEmptyGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.getGroupId()).get();
        assertThat(group.getName()).isEqualTo("John Doe's Group");
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

        assertThat(user.getGroupId()).isEqualTo(existingGroup.getId());
        assertThat(groupRepository.findById(existingGroup.getId()).get().hasMember(user.getId())).isTrue();
        assertThat(invitationRepository.findByToken(invitation.getInvitationToken()).get().getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(groupRepository.size()).isEqualTo(1);
        assertThat(userRepository.size()).isEqualTo(1);
    }

    @Test
    void shouldThrowExceptionWhenInvitationTokenNotFound() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", "invalid-token"
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(InvitationNotFoundException.class);
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

        assertThatThrownBy(() -> userService.registerUser(duplicateCommand))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldFindUserByExternalAuthId() {
        RegisterUserCommand command = new RegisterUserCommand(
            "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );
        User savedUser = userService.registerUser(command);

        User foundUser = userService.findByExternalAuthId("ext-auth-123").get();

        assertThat(foundUser.getId()).isEqualTo(savedUser.getId());
        assertThat(foundUser.getExternalAuthId()).isEqualTo("ext-auth-123");
    }

    @Test
    void shouldReturnEmptyWhenUserNotFoundByExternalAuthId() {
        assertThat(userService.findByExternalAuthId("non-existing")).isEmpty();
    }

    @Test
    void shouldFindGroupMembers() {
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

        assertThat(members1).hasSize(1);
        assertThat(members2).hasSize(1);
        assertThat(members1).contains(user1);
        assertThat(members2).contains(user2);
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

        assertThat(groupRepository.size()).isEqualTo(2);

        userService.transferUserToGroup(user1.getId(), newGroupId);

        User updatedUser1 = userService.findById(user1.getId()).get();
        assertThat(updatedUser1.getGroupId()).isEqualTo(newGroupId);
        
        Group newGroup = groupRepository.findById(newGroupId).get();
        assertThat(newGroup.hasMember(user1.getId())).isTrue();
        assertThat(newGroup.hasMember(user2.getId())).isTrue();
        assertThat(newGroup.getMemberCount()).isEqualTo(2);

        assertThat(groupRepository.existsById(oldGroupId)).isFalse();
        assertThat(groupRepository.size()).isEqualTo(1);
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

        assertThat(groupRepository.existsById(targetGroupId)).isTrue();
        Group remainingGroup = groupRepository.findById(targetGroupId).get();
        assertThat(remainingGroup.hasMember(user2.getId())).isTrue();
        assertThat(remainingGroup.getMemberCount()).isEqualTo(1);
    }
}