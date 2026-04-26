package kz.masku.orgmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.masku.orgmanager.model.dto.CreateUserRequest;
import kz.masku.orgmanager.model.dto.UpdateUserRequest;
import kz.masku.orgmanager.model.dto.UserResponse;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * HR module REST API.
 *
 * <pre>
 * RBAC summary:
 *   POST   /users          → ADMIN only
 *   GET    /users          → ADMIN, MANAGER
 *   GET    /users/{id}     → ADMIN or the user themselves
 *   PUT    /users/{id}     → ADMIN only
 *   DELETE /users/{id}     → ADMIN only (soft delete — sets status=INACTIVE)
 *   GET    /users/me       → any authenticated user
 * </pre>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users / HR", description = "Employee lifecycle management")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new employee (ADMIN only)")
    public ResponseEntity<UserResponse> create(@RequestBody @Valid CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "List employees with optional department/role filter")
    public ResponseEntity<List<UserResponse>> getAll(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(userService.getAllUsers(departmentId, role));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isCurrentUser(#id, authentication)")
    @Operation(summary = "Get a user by ID (ADMIN or own profile)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id,
                                                Authentication authentication) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update employee data (ADMIN only)")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @RequestBody @Valid UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate an employee (ADMIN only — soft delete)")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get the currently authenticated user's profile")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication));
    }
}
