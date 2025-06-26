package pl.btsoftware.backend.account.infrastructure.api;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.lang.Nullable;

public record CreateAccountRequest(@NotBlank @Length(max = 100) String name, @Nullable String currency) {
}
