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
    void shouldRegisterUserInNewGroup() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );

        User user = userService.registerUser(command);

        assertThat(user.id()).isNotNull();
        assertThat(user.email()).isEqualTo("test@example.com");
        assertThat(user.displayName()).isEqualTo("John Doe");
        assertThat(user.groupId()).isNotNull();

        assertThat(groupRepository.existsById(user.groupId())).isTrue();
        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("My Group");
        assertThat(group.hasMember(user.id())).isTrue();
        assertThat(groupRepository.size()).isEqualTo(1);
        assertThat(userRepository.size()).isEqualTo(1);
    }

    @Test
    void shouldRegisterUserWithDefaultGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", null, null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("John Doe's Group");
    }

    @Test
    void shouldRegisterUserWithEmptyGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("John Doe's Group");
    }

    @Test
    void shouldRegisterUserToExistingGroup() {
        UserId inviterId = UserId.generate();
        Group existingGroup = Group.create("Existing Group", "Description", inviterId);
        groupRepository.save(existingGroup);

        GroupInvitation invitation = GroupInvitation.create(existingGroup.id(), "test@example.com", inviterId);
        invitationRepository.save(invitation);

        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "Ignored Group", invitation.getInvitationToken()
        );

        User user = userService.registerUser(command);

        assertThat(user.groupId()).isEqualTo(existingGroup.id());
        assertThat(groupRepository.findById(existingGroup.id()).get().hasMember(user.id())).isTrue();
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
    void shouldFindGroupMembers() {
        RegisterUserCommand command1 = new RegisterUserCommand(
                "ext-auth-1", "user1@example.com", "User 1", "Group One", null
        );
        RegisterUserCommand command2 = new RegisterUserCommand(
                "ext-auth-2", "user2@example.com", "User 2", "Group Two", null
        );

        User user1 = userService.registerUser(command1);
        User user2 = userService.registerUser(command2);

        List<User> members1 = userService.findGroupMembers(user1.groupId());
        List<User> members2 = userService.findGroupMembers(user2.groupId());

        assertThat(members1).hasSize(1);
        assertThat(members2).hasSize(1);
        assertThat(members1).contains(user1);
        assertThat(members2).contains(user2);
    }

    @Test
    void shouldAllowDuplicateGroupNameWhenJoiningViaInvitation() {
        UserId inviterId = UserId.generate();
        Group existingGroup = Group.create("Shared Group", "Description", inviterId);
        groupRepository.save(existingGroup);

        GroupInvitation invitation = GroupInvitation.create(existingGroup.id(), "test@example.com", inviterId);
        invitationRepository.save(invitation);

        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "Shared Group", invitation.getInvitationToken()
        );

        User user = userService.registerUser(command);

        assertThat(user.groupId()).isEqualTo(existingGroup.id());
        assertThat(groupRepository.size()).isEqualTo(1);
    }

    @Test
    void shouldRegisterUserWithWhitespaceGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "  ", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("John Doe's Group");
    }

    @Test
    void shouldTrimWhitespaceFromGroupName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "  My Group  ", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("My Group");
    }

    @Test
    void shouldRegisterMultipleUsersWithDifferentExternalAuthIds() {
        RegisterUserCommand command1 = new RegisterUserCommand(
                "ext-auth-123", "user1@example.com", "User One", "Group One", null
        );
        RegisterUserCommand command2 = new RegisterUserCommand(
                "ext-auth-456", "user2@example.com", "User Two", "Group Two", null
        );

        User user1 = userService.registerUser(command1);
        User user2 = userService.registerUser(command2);

        assertThat(user1.id()).isEqualTo(new UserId("ext-auth-123"));
        assertThat(user2.id()).isEqualTo(new UserId("ext-auth-456"));
        assertThat(userRepository.size()).isEqualTo(2);
        assertThat(groupRepository.size()).isEqualTo(2);
    }

    @Test
    void shouldRegisterUserWithLongDisplayName() {
        String longDisplayName = "Very Long Display Name That Contains Many Characters And Should Still Be Valid";
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", longDisplayName, "My Group", null
        );

        User user = userService.registerUser(command);

        assertThat(user.displayName()).isEqualTo(longDisplayName);
    }

    @Test
    void shouldRegisterUserWithSpecialCharactersInName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "Jöhn Döe-Smith", "My Group", null
        );

        User user = userService.registerUser(command);

        assertThat(user.displayName()).isEqualTo("Jöhn Döe-Smith");
    }

    @Test
    void shouldThrowExceptionWhenExternalAuthIdIsNull() {
        RegisterUserCommand command = new RegisterUserCommand(
                null, "test@example.com", "John Doe", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", null, "John Doe", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsEmpty() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "", "John Doe", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsWhitespace() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "   ", "John Doe", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.UserEmailEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsNull() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", null, "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsEmpty() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenDisplayNameIsWhitespace() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "   ", "My Group", null
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException.class);
    }

    @Test
    void shouldThrowExceptionWhenUserAlreadyExistsWithSameExternalAuthId() {
        RegisterUserCommand command1 = new RegisterUserCommand(
                "ext-auth-123", "user1@example.com", "User One", "Group One", null
        );
        userService.registerUser(command1);

        RegisterUserCommand command2 = new RegisterUserCommand(
                "ext-auth-123", "user2@example.com", "User Two", "Group Two", null
        );

        assertThatThrownBy(() -> userService.registerUser(command2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User with external auth ID already exists");
    }

    @Test
    void shouldThrowExceptionWhenInvitationTokenIsInvalid() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", "invalid-token-123"
        );

        assertThatThrownBy(() -> userService.registerUser(command))
                .isInstanceOf(InvitationNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenInvitationTokenIsEmpty() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", ""
        );

        User user = userService.registerUser(command);

        assertThat(user).isNotNull();
        assertThat(groupRepository.size()).isEqualTo(1);
        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("My Group");
    }

    @Test
    void shouldThrowExceptionWhenInvitationTokenIsWhitespace() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", "   "
        );

        User user = userService.registerUser(command);

        assertThat(user).isNotNull();
        assertThat(groupRepository.size()).isEqualTo(1);
        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("My Group");
    }

    @Test
    void shouldCreateDefaultGroupNameWhenDisplayNameContainsSpecialCharacters() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "Jöhn Döe-Smith", null, null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("Jöhn Döe-Smith's Group");
    }

    @Test
    void shouldRegisterUserWhenGroupNameIsSameAsDisplayName() {
        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "John Doe", null
        );

        User user = userService.registerUser(command);

        Group group = groupRepository.findById(user.groupId()).get();
        assertThat(group.name()).isEqualTo("John Doe");
    }

    @Test
    void shouldStoreUserDataCorrectlyAfterRegistration() {
        // given
        var command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );

        // when
        var user = userService.registerUser(command);

        // then
        var storedUser = userRepository.findById(user.id()).get();
        assertThat(storedUser.id()).isEqualTo(user.id());
        assertThat(storedUser.email()).isEqualTo("test@example.com");
        assertThat(storedUser.displayName()).isEqualTo("John Doe");
        assertThat(storedUser.groupId()).isEqualTo(user.groupId());
        assertThat(storedUser.createdAt()).isNotNull();
        assertThat(storedUser.lastLoginAt()).isNotNull();
        assertThat(storedUser.joinedGroupAt()).isNotNull();
    }

    @Test
    void shouldSetCorrectTimestampsOnRegistration() {
        java.time.Instant before = java.time.Instant.now();

        RegisterUserCommand command = new RegisterUserCommand(
                "ext-auth-123", "test@example.com", "John Doe", "My Group", null
        );

        User user = userService.registerUser(command);

        java.time.Instant after = java.time.Instant.now();

        assertThat(user.createdAt()).isNotNull();
        assertThat(user.lastLoginAt()).isNotNull();
        assertThat(user.joinedGroupAt()).isNotNull();

        assertThat(user.createdAt()).isBetween(before, after);
        assertThat(user.lastLoginAt()).isBetween(before, after);
        assertThat(user.joinedGroupAt()).isBetween(before, after);
    }
}