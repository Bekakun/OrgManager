package kz.masku.orgmanager.model.dto;

import jakarta.validation.constraints.*;
import kz.masku.orgmanager.model.enums.Role;

/** Request body for POST /api/users (ADMIN only). */
public record CreateUserRequest(

        @NotBlank
        String fullName,

        @NotBlank @Email
        String email,

        /**
         * Strong-password rules:
         *   (?=.*[A-Z])       — at least one uppercase letter
         *   (?=.*\d)          — at least one digit
         *   (?=.*[@$!%*?&_\-#^+=<>.]) — at least one special character
         *   .{8,}             — minimum 8 characters total
         */
        @NotBlank
        @Pattern(
            regexp  = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_\\-#^+=<>.]).{8,}$",
            message = "Пароль должен содержать минимум 8 символов, одну заглавную букву, цифру и спецсимвол"
        )
        String password,

        String position,

        Long departmentId,

        @NotNull
        Role role
) {}
