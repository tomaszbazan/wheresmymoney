package pl.btsoftware.backend.users.infrastructure.api;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.UserId;

@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final UsersModuleFacade usersModuleFacade;

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupInvitationView inviteToGroup(
            @RequestBody @Validated InviteToGroupRequest request, @RequestParam String inviterId) {
        log.info("Creating invitation for email: {} by inviter: {}", request.email(), inviterId);

        var command = new InviteToGroupCommand(request.email());

        var invitation = usersModuleFacade.inviteToGroup(new UserId(inviterId), command);

        log.info("Invitation created with token: {}", invitation.invitationToken());
        return GroupInvitationView.from(invitation);
    }

    @GetMapping("/invitation/{token}")
    public GroupInvitationView getInvitationDetails(@PathVariable String token) {
        log.info("Getting invitation details for token: {}", token);

        var invitation = usersModuleFacade
                .findInvitationByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));

        return GroupInvitationView.from(invitation);
    }

    @PostMapping("/invitation/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(@PathVariable String token, @RequestParam String userId) {
        log.info("Accepting invitation with token: {} by user: {}", token, userId);

        usersModuleFacade.acceptInvitation(token, new UserId(userId));

        log.info("Invitation accepted successfully");
    }

    @GetMapping("/{groupId}")
    public GroupView getGroup(@PathVariable UUID groupId) {
        log.info("Getting group details for ID: {}", groupId);

        var group = usersModuleFacade
                .findGroupById(new GroupId(groupId))
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        return GroupView.from(group);
    }
}
