package kz.masku.orgmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.masku.orgmanager.model.entity.AuditLog;
import kz.masku.orgmanager.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only audit log endpoint — ADMIN access only.
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Audit Logs", description = "Immutable audit trail (ADMIN only)")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @Operation(summary = "Paginated list of all audit entries (newest first)")
    public ResponseEntity<Page<AuditLog>> getAll(
            @PageableDefault(size = 50) Pageable pageable) {
        return ResponseEntity.ok(
                auditLogRepository.findAllByOrderByCreatedAtDesc(pageable));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "All audit entries for a specific user")
    public ResponseEntity<List<AuditLog>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(
                auditLogRepository.findByUserIdOrderByCreatedAtDesc(userId));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "All audit entries for a specific entity")
    public ResponseEntity<List<AuditLog>> getByEntity(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(
                auditLogRepository.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
                        entityType, entityId));
    }
}
