package kz.masku.orgmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.masku.orgmanager.model.dto.*;
import kz.masku.orgmanager.service.DocumentService;
import kz.masku.orgmanager.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Document module REST API.
 *
 * <pre>
 * RBAC summary:
 *   POST  /documents                              → any authenticated user
 *   GET   /documents                              → ADMIN, MANAGER
 *   GET   /documents/{id}                         → owner / manager of dept / ADMIN
 *   POST  /documents/{id}/submit                  → document author or ADMIN
 *   POST  /documents/{id}/approvals/{aId}/decide  → assigned approver or ADMIN
 *   POST  /documents/{id}/return-to-draft         → ADMIN or MANAGER
 * </pre>
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document lifecycle and approval workflow")
public class DocumentController {

    private final DocumentService  documentService;
    private final WorkflowService  workflowService;

    // ── CRUD ──────────────────────────────────────────────────────────────────

    /**
     * Creates a new document in DRAFT status.
     * Accepts a multipart request: {@code data} (JSON) + optional {@code file}.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create a document (optionally with a file attachment)")
    public ResponseEntity<DocumentResponse> create(
            @RequestPart("data") @Valid CreateDocumentRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.createDocument(request, file, authentication));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "List documents visible to the current user")
    public ResponseEntity<List<DocumentResponse>> getAll(Authentication authentication) {
        return ResponseEntity.ok(
                documentService.getAllDocuments(authentication, null,
                        org.springframework.data.domain.Pageable.unpaged()).getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get a single document (access checked by role)")
    public ResponseEntity<DocumentResponse> getById(@PathVariable Long id,
                                                    Authentication authentication) {
        return ResponseEntity.ok(documentService.getDocumentById(id, authentication));
    }

    // ── Workflow transitions ───────────────────────────────────────────────────

    /**
     * DRAFT → PENDING: submits the document for approval and assigns approvers.
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit a DRAFT document for approval")
    public ResponseEntity<DocumentResponse> submitForApproval(
            @PathVariable Long id,
            @RequestBody @Valid SubmitForApprovalRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                workflowService.submitForApproval(id, request, authentication));
    }

    /**
     * PENDING → APPROVED / REJECTED: an assigned approver records their decision.
     */
    @PostMapping("/{id}/approvals/{approvalId}/decide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Record an approver's decision (APPROVED or REJECTED)")
    public ResponseEntity<DocumentResponse> decide(
            @PathVariable Long id,
            @PathVariable Long approvalId,
            @RequestBody @Valid ApprovalDecisionRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                workflowService.processApproval(id, approvalId, request, authentication));
    }

    /**
     * REJECTED → DRAFT: returns a rejected document for revision, increments version.
     */
    @PostMapping("/{id}/return-to-draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Return a REJECTED document to DRAFT (version++)")
    public ResponseEntity<DocumentResponse> returnToDraft(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(workflowService.returnToDraft(id, authentication));
    }
}
