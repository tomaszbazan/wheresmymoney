package pl.btsoftware.backend.users.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.config.WebConfig;
import pl.btsoftware.backend.users.UsersModuleFacade;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;
import pl.btsoftware.backend.users.domain.error.UserNotFoundException;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, UserExceptionHandler.class})
@Import(WebConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UsersModuleFacade usersModuleFacade;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        // given
        var request = Instancio.create(RegisterUserRequest.class);

        var user = Instancio.of(User.class)
                .set(field(User::id), new UserId(request.externalAuthId()))
                .set(field(User::email), request.email())
                .set(field(User::displayName), request.displayName())
                .create();
        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class))).thenReturn(user);

        // when & then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(user.id().value()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.displayName").value(user.displayName()))
                .andExpect(jsonPath("$.groupId").value(user.groupId().value().toString()))
                .andExpect(jsonPath("$.createdAt").value(user.createdAt().toString()))
                .andExpect(jsonPath("$.lastLoginAt").value(user.lastLoginAt().toString()))
                .andExpect(jsonPath("$.joinedGroupAt").value(user.joinedGroupAt().toString()));
    }

    @Test
    void shouldRegisterUserWithInvitationToken() throws Exception {
        // given
        var request = Instancio.create(RegisterUserRequest.class);

        var mockUser = Instancio.of(User.class)
                .set(field(User::id), new UserId(request.externalAuthId()))
                .set(field(User::email), request.email())
                .set(field(User::displayName), request.displayName())
                .create();
        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class))).thenReturn(mockUser);

        // when & then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("invitationToken", "valid-token-123"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(mockUser.id().value()))
                .andExpect(jsonPath("$.email").value(mockUser.email()))
                .andExpect(jsonPath("$.displayName").value(mockUser.displayName()))
                .andExpect(jsonPath("$.groupId").value(mockUser.groupId().value().toString()))
                .andExpect(jsonPath("$.createdAt").value(mockUser.createdAt().toString()))
                .andExpect(jsonPath("$.lastLoginAt").value(mockUser.lastLoginAt().toString()))
                .andExpect(jsonPath("$.joinedGroupAt").value(mockUser.joinedGroupAt().toString()));
    }

    @Test
    void shouldGetUserProfileSuccessfully() throws Exception {
        // given
        var user = Instancio.create(User.class);
        when(usersModuleFacade.findUserOrThrow(new UserId(user.id().value()))).thenReturn(user);

        mockMvc.perform(get("/api/users/profile")
                        .with(jwt().jwt(jwt -> jwt.subject(user.id().value()))))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$.id").value(user.id().value()))
                .andExpect(jsonPath("$.email").value(user.email()))
                .andExpect(jsonPath("$.displayName").value(user.displayName()))
                .andExpect(jsonPath("$.groupId").value(user.groupId().value().toString()))
                .andExpect(jsonPath("$.createdAt").value(user.createdAt().toString()))
                .andExpect(jsonPath("$.lastLoginAt").value(user.lastLoginAt().toString()))
                .andExpect(jsonPath("$.joinedGroupAt").value(user.joinedGroupAt().toString()));
    }

    @Test
    void shouldReturn400WhenUserNotFound() throws Exception {
        // given
        var externalAuthId = "non-existent";
        when(usersModuleFacade.findUserOrThrow(new UserId(externalAuthId))).thenThrow(new UserNotFoundException());

        // when & then
        mockMvc.perform(get("/api/users/profile")
                .with(jwt().jwt(jwt -> jwt.subject(externalAuthId))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    void shouldReturn500WhenRegisteringUserWithInvalidEmail() throws Exception {
        // given
        var request = Instancio.of(RegisterUserRequest.class).set(field(RegisterUserRequest::email), "").create();
        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class))).thenThrow(new UserEmailEmptyException());

        // when & then
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User email cannot be empty"));
    }

    @Test
    void shouldReturn500WhenRegisteringUserWithInvalidDisplayName() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("ext-auth-123", "test@example.com", "", "Group");

        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new DisplayNameEmptyException());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Display name cannot be empty"));
    }

    @Test
    void shouldReturn404WhenInvitationTokenNotFound() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "ext-auth-999",
            "test@example.com",
            "Test User",
            "Test Group"
        );

        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new InvitationNotFoundException());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("invitationToken", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invitation not found"));
    }

    @Test
    void shouldReturn409WhenUserAlreadyExists() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "ext-auth-duplicate",
            "duplicate@example.com",
            "Duplicate User",
            "Test Group"
        );

        when(usersModuleFacade.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new IllegalStateException("User with external auth ID already exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("User with external auth ID already exists"));
    }

    @Test
    void shouldReturnUnauthorizedWhenAccessingProfileWithoutToken() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn500WhenRegisterRequestHasNullValues() throws Exception {
        String invalidJson = "{ \"invalidField\": \"value\" }";

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }
}
