package kz.masku.orgmanager.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** POST /api/auth/login request body. */
public record LoginRequest(

        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {}
