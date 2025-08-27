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
        var externalAuthId1 = "ext-auth-" + UUID.randomUUID();
        var registerCommand = new RegisterUserCommand(
                externalAuthId1,
                "user1@example.com",
                "John Doe",
                "John's Family Group",
                null
        );

        var registeredUser = usersModuleFacade.registerUser(registerCommand);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.getExternalAuthId()).isEqualTo(externalAuthId1);
        assertThat(registeredUser.getEmail()).isEqualTo("user1@example.com");
        assertThat(registeredUser.getDisplayName()).isEqualTo("John Doe");
        assertThat(registeredUser.getGroupId()).isNotNull();

        // 2. Sprawdzenie czy grupa została poprawnie utworzona
        var groupId = registeredUser.getGroupId();
        var createdGroup = usersModuleFacade.findGroupById(groupId)
                .orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(createdGroup).isNotNull();
        assertThat(createdGroup.getName()).isEqualTo("John's Family Group");
        assertThat(createdGroup.getMemberCount()).isEqualTo(1);
        assertThat(createdGroup.getMemberIds()).contains(registeredUser.getId());
        assertThat(createdGroup.getCreatedBy()).isEqualTo(registeredUser.getId());

        // 3. Pobranie profilu użytkownika
        var userProfile = usersModuleFacade.findUserByExternalAuthId(externalAuthId1)
                .orElseThrow(() -> new AssertionError("User should exist"));

        assertThat(userProfile).isNotNull();
        assertThat(userProfile.getId()).isEqualTo(registeredUser.getId());
        assertThat(userProfile.getExternalAuthId()).isEqualTo(externalAuthId1);
        assertThat(userProfile.getEmail()).isEqualTo("user1@example.com");
        assertThat(userProfile.getDisplayName()).isEqualTo("John Doe");
        assertThat(userProfile.getGroupId()).isEqualTo(groupId);

        // 4. Wysłanie zaproszenia do grupy
        var inviteCommand = new InviteToGroupCommand("user2@example.com");

        var invitation = usersModuleFacade.inviteToGroup(registeredUser.getId(), inviteCommand);

        assertThat(invitation).isNotNull();
        assertThat(invitation.getInviteeEmail()).isEqualTo("user2@example.com");
        assertThat(invitation.getGroupId()).isEqualTo(groupId);
        assertThat(invitation.getInvitedBy()).isEqualTo(registeredUser.getId());
        assertThat(invitation.getInvitationToken()).isNotNull();
        assertThat(invitation.isPending()).isTrue();
        assertThat(invitation.isExpired()).isFalse();

        // 5. Utworzenie drugiego użytkownika w grupie (przez zaproszenie)
        var externalAuthId2 = "ext-auth-" + UUID.randomUUID();
        var registerCommand2 = new RegisterUserCommand(
                externalAuthId2,
                "user2@example.com",
                "Jane Smith",
                "Jane's Group",  // Ta nazwa będzie zignorowana bo użytkownik dołącza przez zaproszenie
                invitation.getInvitationToken()
        );

        var secondUser = usersModuleFacade.registerUser(registerCommand2);

        assertThat(secondUser).isNotNull();
        assertThat(secondUser.getExternalAuthId()).isEqualTo(externalAuthId2);
        assertThat(secondUser.getEmail()).isEqualTo("user2@example.com");
        assertThat(secondUser.getDisplayName()).isEqualTo("Jane Smith");
        assertThat(secondUser.getGroupId()).isEqualTo(groupId);  // Powinna być w tej samej grupie

        // Weryfikacja końcowego stanu grupy
        var finalGroup = usersModuleFacade.findGroupById(groupId)
                .orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(finalGroup).isNotNull();
        assertThat(finalGroup.getName()).isEqualTo("John's Family Group");  // Nazwa się nie zmieniła
        assertThat(finalGroup.getMemberCount()).isEqualTo(2);  // Teraz 2 członków
        assertThat(finalGroup.getMemberIds()).contains(registeredUser.getId(), secondUser.getId());

        // Weryfikacja że zaproszenie zostało zaakceptowane
        var finalInvitation = usersModuleFacade.findInvitationByToken(invitation.getInvitationToken())
                .orElseThrow(() -> new AssertionError("Invitation should exist"));

        assertThat(finalInvitation).isNotNull();
        assertThat(finalInvitation.isPending()).isFalse();
        assertThat(finalInvitation.getStatus().name()).isEqualTo("ACCEPTED");
    }

}
