package kz.masku.orgmanager.model.dto;

import kz.masku.orgmanager.model.entity.User;

/** Safe projection of a User — passwordHash is intentionally excluded. */
public record UserResponse(

        Long id,
        String fullName,
        String email,
        String position,
        Long departmentId,
        String departmentName,
        String role,
        String status
) {
    /**
     * Factory method: converts a managed {@link User} entity to this DTO.
     * Call inside an active transaction to safely access lazy {@code department}.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPosition(),
                user.getDepartment() != null ? user.getDepartment().getId()   : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getRole().name(),
                user.getStatus().name()
        );
    }
}
