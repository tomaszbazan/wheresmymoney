package pl.btsoftware.backend.account.infrastructure.api;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UpdateAccountRequest(
        @NotBlank @Length(max = 100) String name) {}
