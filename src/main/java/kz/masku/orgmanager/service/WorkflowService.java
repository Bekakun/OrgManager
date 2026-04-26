package kz.masku.orgmanager.service;

import kz.masku.orgmanager.audit.Auditable;
import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.exception.ResourceNotFoundException;
import kz.masku.orgmanager.model.dto.ApprovalDecisionRequest;
import kz.masku.orgmanager.model.dto.DocumentResponse;
import kz.masku.orgmanager.model.dto.SubmitForApprovalRequest;
import kz.masku.orgmanager.model.entity.Document;
import kz.masku.orgmanager.model.entity.DocumentApproval;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.ApprovalDecision;
import kz.masku.orgmanager.model.enums.DocumentStatus;
import kz.masku.orgmanager.repository.DocumentApprovalRepository;
import kz.masku.orgmanager.repository.DocumentRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Document workflow state machine.
 *
 * <pre>
 *   DRAFT ──submit──► PENDING ──all approve──► APPROVED
 *                        │
 *                        └──any reject──► REJECTED ──return──► DRAFT (version++)
 * </pre>
 *
 * This service is the single entry point for all status transitions.
 * No other class is allowed to call {@code document.setStatus()}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class WorkflowService {

    private final DocumentRepository         documentRepository;
    private final DocumentApprovalRepository approvalRepository;
    private final UserRepository             userRepository;
    private final DocumentService            documentService;
    private final AuditService               auditService;

    // ── Transition 1: DRAFT → PENDING ────────────────────────────────────────

    /**
     * Submits a {@link DocumentStatus#DRAFT} document for approval.
     * Creates one {@link DocumentApproval} record per approver.
     *
     * @param documentId ID of the document to submit
     * @param request    list of approver user IDs
     * @param auth       authentication of the user initiating the submission
     * @return updated document DTO with PENDING status
     * @throws BusinessException     if the document is not in DRAFT state
     * @throws AccessDeniedException if the current user is not the author
     */
    @Auditable(action = "DOCUMENT_SUBMITTED", entityType = "Document")
    public DocumentResponse submitForApproval(Long documentId,
                                              SubmitForApprovalRequest request,
                                              Authentication auth) {
        Document doc     = documentService.findById(documentId);
        User     current = documentService.resolveCurrentUser(auth);

        // Only the document's author or an ADMIN may submit
        if (!doc.getAuthor().getId().equals(current.getId())
                && !documentService.hasRole(auth, "ROLE_ADMIN")) {
            throw new AccessDeniedException("Only the document author may submit it for approval");
        }

        if (doc.getStatus() != DocumentStatus.DRAFT) {
            throw new BusinessException(
                    "Document must be in DRAFT to submit. Current status: " + doc.getStatus());
        }

        // Create one approval slot per requested approver
        for (Long approverId : request.approverIds()) {
            User approver = userRepository.findById(approverId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Approver not found: " + approverId));

            DocumentApproval approval = DocumentApproval.builder()
                    .document(doc)
                    .approver(approver)
                    .decision(ApprovalDecision.PENDING)
                    .build();

            approvalRepository.save(approval);
        }

        doc.setStatus(DocumentStatus.PENDING);
        documentRepository.save(doc);

        auditService.log(current.getId(), "DOCUMENT_SUBMITTED", "Document", doc.getId(),
                "approvers=" + request.approverIds());

        return DocumentResponse.from(doc);
    }

    // ── Transition 2: PENDING → APPROVED / REJECTED ───────────────────────────

    /**
     * Records an approver's decision and recomputes the document's overall status.
     *
     * <ul>
     *   <li>If any approver rejects → document becomes {@link DocumentStatus#REJECTED}.</li>
     *   <li>If all approvers approve → document becomes {@link DocumentStatus#APPROVED}.</li>
     *   <li>Otherwise the document remains {@link DocumentStatus#PENDING}.</li>
     * </ul>
     *
     * @param documentId ID of the document being decided upon
     * @param approvalId ID of the specific approval record
     * @param request    APPROVED or REJECTED + optional comment
     * @param auth       authentication of the approver
     * @return updated document DTO
     * @throws BusinessException     if the document is not PENDING or the approval was already decided
     * @throws AccessDeniedException if the current user is not the assigned approver
     */
    @Auditable(action = "APPROVAL_DECISION", entityType = "Document")
    public DocumentResponse processApproval(Long documentId,
                                            Long approvalId,
                                            ApprovalDecisionRequest request,
                                            Authentication auth) {
        Document doc     = documentService.findById(documentId);
        User     current = documentService.resolveCurrentUser(auth);

        if (doc.getStatus() != DocumentStatus.PENDING) {
            throw new BusinessException("Document is not in PENDING status. Current: " + doc.getStatus());
        }

        DocumentApproval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Approval record not found: " + approvalId));

        // Verify that this approval belongs to the correct document
        if (!approval.getDocument().getId().equals(documentId)) {
            throw new BusinessException("Approval #" + approvalId + " does not belong to document #" + documentId);
        }

        // Only the assigned approver (or ADMIN) may decide
        if (!approval.getApprover().getId().equals(current.getId())
                && !documentService.hasRole(auth, "ROLE_ADMIN")) {
            throw new AccessDeniedException("You are not assigned as an approver for this document");
        }

        if (approval.getDecision() != ApprovalDecision.PENDING) {
            throw new BusinessException("This approval has already been decided: " + approval.getDecision());
        }

        // Record the decision
        approval.setDecision(request.decision());
        approval.setComment(request.comment());
        approval.setDecidedAt(LocalDateTime.now());
        approvalRepository.save(approval);

        // Recompute document-level status from all approval records
        recalculateDocumentStatus(doc);

        auditService.log(current.getId(), "APPROVAL_DECISION", "Document", doc.getId(),
                "approvalId=" + approvalId + " decision=" + request.decision()
                        + " comment=" + request.comment());

        return DocumentResponse.from(doc);
    }

    // ── Transition 3: REJECTED → DRAFT ────────────────────────────────────────

    /**
     * Returns a {@link DocumentStatus#REJECTED} document to {@link DocumentStatus#DRAFT}
     * for revision, incrementing the version counter and clearing all approval records.
     *
     * @param documentId document to reset
     * @param auth       authentication of the user initiating the return
     * @return updated document DTO with DRAFT status and incremented version
     * @throws BusinessException if the document is not in REJECTED state
     */
    @Auditable(action = "DOCUMENT_RETURNED_TO_DRAFT", entityType = "Document")
    public DocumentResponse returnToDraft(Long documentId, Authentication auth) {
        Document doc     = documentService.findById(documentId);
        User     current = documentService.resolveCurrentUser(auth);

        if (doc.getStatus() != DocumentStatus.REJECTED) {
            throw new BusinessException(
                    "Only REJECTED documents can be returned to DRAFT. Current: " + doc.getStatus());
        }

        // Clear stale approval records before a new round
        approvalRepository.deleteByDocument(doc);

        doc.setVersion(doc.getVersion() + 1);
        doc.setStatus(DocumentStatus.DRAFT);
        documentRepository.save(doc);

        auditService.log(current.getId(), "DOCUMENT_RETURNED_TO_DRAFT", "Document", doc.getId(),
                "newVersion=" + doc.getVersion());

        return DocumentResponse.from(doc);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Derives the document's aggregate status from all individual approval decisions.
     * Rule: any REJECTED decision wins; only all-APPROVED flips to APPROVED.
     */
    private void recalculateDocumentStatus(Document doc) {
        List<DocumentApproval> approvals = approvalRepository.findByDocument(doc);

        boolean anyRejected = approvals.stream()
                .anyMatch(a -> a.getDecision() == ApprovalDecision.REJECTED);

        boolean allApproved = !approvals.isEmpty() && approvals.stream()
                .allMatch(a -> a.getDecision() == ApprovalDecision.APPROVED);

        if (anyRejected) {
            doc.setStatus(DocumentStatus.REJECTED);
        } else if (allApproved) {
            doc.setStatus(DocumentStatus.APPROVED);
        }
        // else: some are still PENDING — document remains PENDING

        documentRepository.save(doc);
    }
}
