package pl.btsoftware.backend.users;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.btsoftware.backend.configuration.SystemTest;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.UserId;

@SystemTest
public class UserGroupTest {

    @Autowired
    private UsersModuleFacade usersModuleFacade;

    @Test
    void shouldCompleteFullUserRegistrationAndGroupInvitationFlow() {
        // 1. Rejestracja użytkownika
        var externalAuthId1 = "ext-auth-" + UUID.randomUUID();
        var registerCommand =
                new RegisterUserCommand(externalAuthId1, "user1@example.com", "John Doe", "Johns Family Group", null);

        var registeredUser = usersModuleFacade.registerUser(registerCommand);

        assertThat(registeredUser).isNotNull();
        assertThat(registeredUser.id().value()).isEqualTo(externalAuthId1);
        assertThat(registeredUser.email()).isEqualTo("user1@example.com");
        assertThat(registeredUser.displayName()).isEqualTo("John Doe");
        assertThat(registeredUser.groupId()).isNotNull();

        // 2. Sprawdzenie czy grupa została poprawnie utworzona
        var groupId = registeredUser.groupId();
        var createdGroup =
                usersModuleFacade.findGroupById(groupId).orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(createdGroup).isNotNull();
        assertThat(createdGroup.name()).isEqualTo("Johns Family Group");
        assertThat(createdGroup.getMemberCount()).isEqualTo(1);
        assertThat(createdGroup.memberIds()).contains(registeredUser.id());
        assertThat(createdGroup.createdBy()).isEqualTo(registeredUser.id());

        // 3. Pobranie profilu użytkownika
        var userProfile = usersModuleFacade.findUserOrThrow(new UserId(externalAuthId1));

        assertThat(userProfile).isNotNull();
        assertThat(userProfile.id().value()).isEqualTo(registeredUser.id().value());
        assertThat(userProfile.email()).isEqualTo("user1@example.com");
        assertThat(userProfile.displayName()).isEqualTo("John Doe");
        assertThat(userProfile.groupId().value()).isEqualTo(groupId.value());

        // 4. Wysłanie zaproszenia do grupy
        var inviteCommand = new InviteToGroupCommand("user2@example.com");

        var invitation = usersModuleFacade.inviteToGroup(registeredUser.id(), inviteCommand);

        assertThat(invitation).isNotNull();
        assertThat(invitation.inviteeEmail()).isEqualTo("user2@example.com");
        assertThat(invitation.groupId()).isEqualTo(groupId);
        assertThat(invitation.invitedBy()).isEqualTo(registeredUser.id());
        assertThat(invitation.invitationToken()).isNotNull();
        assertThat(invitation.isPending()).isTrue();
        assertThat(invitation.isExpired()).isFalse();

        // 5. Utworzenie drugiego użytkownika w grupie (przez zaproszenie)
        var externalAuthId2 = "ext-auth-" + UUID.randomUUID();
        var registerCommand2 = new RegisterUserCommand(
                externalAuthId2, "user2@example.com", "Jane Smith", "Janes Group", invitation.invitationToken());

        var secondUser = usersModuleFacade.registerUser(registerCommand2);

        assertThat(secondUser).isNotNull();
        assertThat(secondUser.id().value()).isEqualTo(externalAuthId2);
        assertThat(secondUser.email()).isEqualTo("user2@example.com");
        assertThat(secondUser.displayName()).isEqualTo("Jane Smith");
        assertThat(secondUser.groupId()).isEqualTo(groupId); // Powinna być w tej samej grupie

        // Weryfikacja końcowego stanu grupy
        var finalGroup =
                usersModuleFacade.findGroupById(groupId).orElseThrow(() -> new AssertionError("Group should exist"));

        assertThat(finalGroup).isNotNull();
        assertThat(finalGroup.name()).isEqualTo("Johns Family Group");
        assertThat(finalGroup.getMemberCount()).isEqualTo(2);
        assertThat(finalGroup.memberIds()).contains(registeredUser.id(), secondUser.id());

        // Weryfikacja że zaproszenie zostało zaakceptowane
        var finalInvitation = usersModuleFacade
                .findInvitationByToken(invitation.invitationToken())
                .orElseThrow(() -> new AssertionError("Invitation should exist"));

        assertThat(finalInvitation).isNotNull();
        assertThat(finalInvitation.isPending()).isFalse();
        assertThat(finalInvitation.status().name()).isEqualTo("ACCEPTED");
    }
}
