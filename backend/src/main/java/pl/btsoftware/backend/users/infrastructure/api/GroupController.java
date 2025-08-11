package pl.btsoftware.backend.users.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.users.application.GroupService;
import pl.btsoftware.backend.users.application.InviteToGroupCommand;
import pl.btsoftware.backend.users.application.UpdateGroupCommand;
import pl.btsoftware.backend.users.application.UserService;
import pl.btsoftware.backend.users.domain.*;

@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
@Slf4j
public class GroupController {
    private final GroupService groupService;
    private final UserService userService;

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupInvitationView inviteToGroup(@RequestBody @Validated InviteToGroupRequest request,
                                            @RequestParam String inviterId) {
        log.info("Creating invitation for email: {} by inviter: {}", request.getEmail(), inviterId);
        
        UserId inviterUserId = UserId.of(inviterId);
        InviteToGroupCommand command = new InviteToGroupCommand(request.getEmail());
        
        GroupInvitation invitation = groupService.inviteToGroup(inviterUserId, command);
        
        log.info("Invitation created with token: {}", invitation.getInvitationToken());
        return GroupInvitationView.from(invitation);
    }

    @GetMapping("/invitation/{token}")
    public GroupInvitationView getInvitationDetails(@PathVariable String token) {
        log.info("Getting invitation details for token: {}", token);
        
        GroupInvitation invitation = groupService.findInvitationByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invitation not found"));
        
        return GroupInvitationView.from(invitation);
    }

    @PostMapping("/invitation/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void acceptInvitation(@PathVariable String token,
                               @RequestParam String userId) {
        log.info("Accepting invitation with token: {} by user: {}", token, userId);
        
        UserId userIdObj = UserId.of(userId);
        groupService.acceptInvitation(token, userIdObj);
        
        log.info("Invitation accepted successfully");
    }

    @GetMapping("/{groupId}")
    public GroupView getGroup(@PathVariable String groupId) {
        log.info("Getting group details for ID: {}", groupId);
        
        GroupId groupIdObj = GroupId.of(groupId);
        Group group = groupService.findGroupById(groupIdObj)
            .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        return GroupView.from(group);
    }

    @PutMapping("/{groupId}")
    public GroupView updateGroup(@PathVariable String groupId,
                               @RequestBody @Validated UpdateGroupRequest request) {
        log.info("Updating group: {}", groupId);
        
        GroupId groupIdObj = GroupId.of(groupId);
        UpdateGroupCommand command = new UpdateGroupCommand(request.getName(), request.getDescription());
        
        Group group = groupService.updateGroup(groupIdObj, command);
        
        log.info("Group updated successfully: {}", groupId);
        return GroupView.from(group);
    }
}