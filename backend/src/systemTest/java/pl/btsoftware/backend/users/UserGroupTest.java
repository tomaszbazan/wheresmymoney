package pl.btsoftware.backend.users;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.application.RegisterUserCommand;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SystemTest
public class UserGroupTest {

    @Autowired
    private UsersModuleFacade usersModuleFacade;

    @Test
    void shouldCompleteFullUserRegistrationAndGroupInvitationFlow() {
        // 1. Rejestracja użytkownika
        var externalAuthId1 = new ExternalAuthId("ext-auth-" + UUID.randomUUID());
        var registerCommand = new RegisterUserCommand(
                externalAuthId1.value(),
                "user1@example.com",
                "John Doe",
                "John's Family Group",
                null
        );

        var registeredUser = usersModuleFacade.registerUser(registerCommand);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getExternalAuthId()).isEqualTo(externalAuthId1);
        assertThat(registeredUser.email()).isEqualTo("user1@example.com");
        assertThat(registeredUser.displayName()).isEqualTo("John Doe");
        assertThat(registeredUser.groupId()).isNotNull();

        // 2. Sprawdzenie czy grupa została poprawnie utworzona
        var groupId = registeredUser.groupId();
        var createdGroup = usersModuleFacade.findGroupById(groupId)
                .orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(createdGroup).isNotNull();
        assertThat(createdGroup.name()).isEqualTo("John's Family Group");
        assertThat(createdGroup.getMemberCount()).isEqualTo(1);
        assertThat(createdGroup.memberIds()).contains(registeredUser.id());
        assertThat(createdGroup.createdBy()).isEqualTo(registeredUser.id());

        // 3. Pobranie profilu użytkownika
        var userProfile = usersModuleFacade.findUserByExternalAuthId(externalAuthId1)
                .orElseThrow(() -> new AssertionError("User should exist"));

        assertThat(userProfile).isNotNull();
        assertThat(userProfile.id()).isEqualTo(registeredUser.id());
        assertThat(userProfile.getExternalAuthId()).isEqualTo(externalAuthId1);
        assertThat(userProfile.email()).isEqualTo("user1@example.com");
        assertThat(userProfile.displayName()).isEqualTo("John Doe");
        assertThat(userProfile.groupId()).isEqualTo(groupId);

        // 4. Wysłanie zaproszenia do grupy
        var inviteCommand = new InviteToGroupCommand("user2@example.com");

        var invitation = usersModuleFacade.inviteToGroup(registeredUser.id(), inviteCommand);

        assertThat(invitation).isNotNull();
        assertThat(invitation.getInviteeEmail()).isEqualTo("user2@example.com");
        assertThat(invitation.getGroupId()).isEqualTo(groupId);
        assertThat(invitation.getInvitedBy()).isEqualTo(registeredUser.id());
        assertThat(invitation.getInvitationToken()).isNotNull();
        assertThat(invitation.isPending()).isTrue();
        assertThat(invitation.isExpired()).isFalse();

        // 5. Utworzenie drugiego użytkownika w grupie (przez zaproszenie)
        var externalAuthId2 = new ExternalAuthId("ext-auth-" + UUID.randomUUID());
        var registerCommand2 = new RegisterUserCommand(
                externalAuthId2.value(),
                "user2@example.com",
                "Jane Smith",
                "Jane's Group",
                invitation.getInvitationToken()
        );

        var secondUser = usersModuleFacade.registerUser(registerCommand2);

        assertThat(secondUser).isNotNull();
        assertThat(secondUser.getExternalAuthId()).isEqualTo(externalAuthId2);
        assertThat(secondUser.email()).isEqualTo("user2@example.com");
        assertThat(secondUser.displayName()).isEqualTo("Jane Smith");
        assertThat(secondUser.groupId()).isEqualTo(groupId);  // Powinna być w tej samej grupie

        // Weryfikacja końcowego stanu grupy
        var finalGroup = usersModuleFacade.findGroupById(groupId)
                .orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(finalGroup).isNotNull();
        assertThat(finalGroup.name()).isEqualTo("John's Family Group");
        assertThat(finalGroup.getMemberCount()).isEqualTo(2);
        assertThat(finalGroup.memberIds()).contains(registeredUser.id(), secondUser.id());

        // Weryfikacja że zaproszenie zostało zaakceptowane
        var finalInvitation = usersModuleFacade.findInvitationByToken(invitation.getInvitationToken())
                .orElseThrow(() -> new AssertionError("Invitation should exist"));

        assertThat(finalInvitation).isNotNull();
        assertThat(finalInvitation.isPending()).isFalse();
        assertThat(finalInvitation.getStatus().name()).isEqualTo("ACCEPTED");
    }

}
