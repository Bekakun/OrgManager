package kz.masku.orgmanager.model.enums;

/**
 * Workflow state machine for documents:
 *   DRAFT → PENDING → APPROVED
 *                   → REJECTED → DRAFT (revision, version++)
 */
public enum DocumentStatus {
    DRAFT,
    PENDING,
    APPROVED,
    REJECTED
}
