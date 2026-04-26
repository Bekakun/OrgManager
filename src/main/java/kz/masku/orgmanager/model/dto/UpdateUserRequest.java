package kz.masku.orgmanager.model.dto;

import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;

/** Request body for PUT /api/users/{id} — all fields optional (partial update). */
public record UpdateUserRequest(
        String fullName,
        String position,
        Long departmentId,
        Role role,
        UserStatus status
) {}
