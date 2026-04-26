package kz.masku.orgmanager.service;

import kz.masku.orgmanager.audit.Auditable;
import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.exception.ResourceNotFoundException;
import kz.masku.orgmanager.model.dto.CreateUserRequest;
import kz.masku.orgmanager.model.dto.UpdateUserRequest;
import kz.masku.orgmanager.model.dto.UserResponse;
import kz.masku.orgmanager.model.entity.Department;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;
import kz.masku.orgmanager.repository.DepartmentRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * HR module: employee lifecycle management.
 * All write methods require ADMIN role (enforced at controller level via @PreAuthorize).
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository       userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder      passwordEncoder;
    private final AuditService         auditService;

    /**
     * Creates a new employee account. Email uniqueness is validated before saving.
     *
     * @param request validated request DTO
     * @return the persisted user as a safe response DTO
     * @throws BusinessException if the email is already in use
     */
    @Auditable(action = "USER_CREATED", entityType = "User")
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email already registered: " + request.email());
        }

        Department department = resolveDepartment(request.departmentId());

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .position(request.position())
                .department(department)
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Returns a user by ID.
     *
     * @throws ResourceNotFoundException if no user with this ID exists
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findById(id));
    }

    /**
     * Lists employees with optional filters by department and/or role.
     * Used by ADMIN (all) and MANAGER (own department only, enforced at controller).
     */
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Long departmentId, Role role) {
        List<User> users;

        if (departmentId != null && role != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
            users = userRepository.findByDepartmentAndRole(dept, role);
        } else if (departmentId != null) {
            users = userRepository.findByDepartmentId(departmentId);
        } else if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }

        return users.stream().map(UserResponse::from).toList();
    }

    /**
     * Partially updates an employee record.
     * Only non-null fields in the request are applied.
     */
    @Auditable(action = "USER_UPDATED", entityType = "User")
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = findById(id);

        if (request.fullName()     != null) user.setFullName(request.fullName());
        if (request.position()     != null) user.setPosition(request.position());
        if (request.role()         != null) user.setRole(request.role());
        if (request.status()       != null) user.setStatus(request.status());
        if (request.departmentId() != null) user.setDepartment(resolveDepartment(request.departmentId()));

        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Soft-deletes a user by setting their status to INACTIVE.
     * Hard delete is intentionally not supported to preserve audit integrity.
     */
    @Auditable(action = "USER_DEACTIVATED", entityType = "User")
    public void deactivateUser(Long id) {
        User user = findById(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    /**
     * Returns the profile of the currently authenticated user.
     *
     * @param authentication Spring Security context (email used as principal name)
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + authentication.getName()));
        return UserResponse.from(user);
    }

    /**
     * SpEL helper for {@code @PreAuthorize} expressions:
     * returns {@code true} if the given user ID belongs to the currently authenticated email.
     */
    @Transactional(readOnly = true)
    public boolean isCurrentUser(Long userId, Authentication authentication) {
        return userRepository.findById(userId)
                .map(u -> u.getEmail().equals(authentication.getName()))
                .orElse(false);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Department resolveDepartment(Long departmentId) {
        if (departmentId == null) return null;
        return departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + departmentId));
    }
}
