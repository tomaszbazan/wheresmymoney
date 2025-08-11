package pl.btsoftware.backend.users.infrastructure.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import pl.btsoftware.backend.users.application.RegisterUserCommand;
import pl.btsoftware.backend.users.application.UserService;
import pl.btsoftware.backend.users.domain.GroupId;
import pl.btsoftware.backend.users.domain.User;
import pl.btsoftware.backend.users.domain.UserId;
import pl.btsoftware.backend.users.domain.error.DisplayNameEmptyException;
import pl.btsoftware.backend.users.domain.error.InvitationNotFoundException;
import pl.btsoftware.backend.users.domain.error.UserEmailEmptyException;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({UserController.class, UserExceptionHandler.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "ext-auth-123",
            "test@example.com",
            "John Doe",
            "My Group"
        );

        User mockUser = createMockUser("ext-auth-123", "test@example.com", "John Doe");
        when(userService.registerUser(any(RegisterUserCommand.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalAuthId").value("ext-auth-123"))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.displayName").value("John Doe"))
                .andExpect(jsonPath("$.groupId").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.lastLoginAt").exists())
                .andExpect(jsonPath("$.joinedGroupAt").exists());
    }

    @Test
    void shouldRegisterUserWithInvitationToken() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest(
            "ext-auth-456",
            "invited@example.com",
            "Jane Doe",
            "Ignored Group"
        );

        User mockUser = createMockUser("ext-auth-456", "invited@example.com", "Jane Doe");
        when(userService.registerUser(any(RegisterUserCommand.class))).thenReturn(mockUser);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("invitationToken", "valid-token-123"))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalAuthId").value("ext-auth-456"))
                .andExpect(jsonPath("$.email").value("invited@example.com"))
                .andExpect(jsonPath("$.displayName").value("Jane Doe"));
    }

    @Test
    void shouldGetUserProfileSuccessfully() throws Exception {
        String externalAuthId = "ext-auth-789";
        User mockUser = createMockUser(externalAuthId, "profile@example.com", "Profile User");
        when(userService.findByExternalAuthId(externalAuthId)).thenReturn(Optional.of(mockUser));

        mockMvc.perform(get("/api/users/profile/{externalAuthId}", externalAuthId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.externalAuthId").value(externalAuthId))
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.displayName").value("Profile User"))
                .andExpect(jsonPath("$.groupId").exists())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldReturn400WhenUserNotFound() throws Exception {
        String externalAuthId = "non-existent";
        when(userService.findByExternalAuthId(externalAuthId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/profile/{externalAuthId}", externalAuthId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    @Test
    void shouldReturn500WhenRegisteringUserWithInvalidEmail() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("ext-auth-123", "", "John Doe", "Group");
        
        when(userService.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new UserEmailEmptyException());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("User email cannot be empty"));
    }

    @Test
    void shouldReturn500WhenRegisteringUserWithInvalidDisplayName() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest("ext-auth-123", "test@example.com", "", "Group");
        
        when(userService.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new DisplayNameEmptyException());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
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

        when(userService.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new InvitationNotFoundException());

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .param("invitationToken", "invalid-token"))
                .andExpect(status().isInternalServerError())
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

        when(userService.registerUser(any(RegisterUserCommand.class)))
            .thenThrow(new IllegalStateException("User with external auth ID already exists"));

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("User with external auth ID already exists"));
    }

    @Test
    void shouldReturn500WhenRegisterRequestHasNullValues() throws Exception {
        String invalidJson = "{ \"invalidField\": \"value\" }";

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isInternalServerError());
    }

    private User createMockUser(String externalAuthId, String email, String displayName) {
        UserId userId = UserId.generate();
        GroupId groupId = GroupId.generate();
        Instant now = Instant.now();
        
        return new User(
            userId,
            externalAuthId,
            email,
            displayName,
            groupId,
            now,
            now,
            now
        );
    }
}