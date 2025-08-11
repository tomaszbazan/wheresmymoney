package pl.btsoftware.backend.users.infrastructure.api;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.application.UserService;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserView registerUser(@RequestBody @Validated RegisterUserRequest request,
                                 @RequestParam(required = false) String invitationToken) {
        log.info("Registering user with email: {}, invitationToken present: {}",
                request.getEmail(), invitationToken != null);

        var command = new RegisterUserCommand(
                request.getExternalAuthId(),
                request.getEmail(),
                request.getDisplayName(),
                request.getGroupName(),
                invitationToken
        );

        var user = userService.registerUser(command);

        log.info("User registered successfully with ID: {}", user.getId());
        return UserView.from(user);
    }

    @GetMapping("/profile/{externalAuthId}")
    public UserView getUserProfile(@PathVariable String externalAuthId) {
        log.info("Getting profile for external auth ID: {}", externalAuthId);

        var user = userService.findByExternalAuthId(externalAuthId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return UserView.from(user);
    }
}