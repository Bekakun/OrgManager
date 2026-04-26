package kz.masku.orgmanager.service;

import kz.masku.orgmanager.model.entity.AuditLog;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.repository.AuditLogRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes audit entries to {@code audit_logs}.
 * Uses {@link Propagation#REQUIRES_NEW} so an audit write never rolls back
 * if the calling transaction fails.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository     userRepository;

    /**
     * Records an audit event by user ID.
     *
     * @param userId     ID of the acting user (nullable for system events)
     * @param action     Action label, e.g. {@code "DOCUMENT_CREATED"}
     * @param entityType Entity type string, e.g. {@code "Document"}
     * @param entityId   PK of the affected entity (nullable)
     * @param details    Free-form JSON or text for additional context
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(Long userId, String action, String entityType, Long entityId, String details) {
        User user = (userId != null) ? userRepository.getReferenceById(userId) : null;
        saveEntry(user, action, entityType, entityId, details);
    }

    /**
     * Records an audit event by user email (used from {@link kz.masku.orgmanager.audit.AuditAspect}).
     *
     * @param email email of the acting user, or {@code "SYSTEM"}
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logByEmail(String email, String action, String entityType, Long entityId, String details) {
        User user = userRepository.findByEmail(email).orElse(null);
        saveEntry(user, action, entityType, entityId, details);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void saveEntry(User user, String action, String entityType, Long entityId, String details) {
        AuditLog entry = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(entry);
        log.info("AUDIT | action={} entity={}#{} user={}",
                action, entityType, entityId,
                user != null ? user.getEmail() : "SYSTEM");
    }
}
