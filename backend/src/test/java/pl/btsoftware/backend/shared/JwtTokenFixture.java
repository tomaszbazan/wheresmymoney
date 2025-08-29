package pl.btsoftware.backend.shared;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public class JwtTokenFixture {
    public static SecurityMockMvcRequestPostProcessors.@NotNull JwtRequestPostProcessor createTokenFor(String userId) {
        return jwt().jwt(jwt -> jwt.subject(userId));
    }
}
