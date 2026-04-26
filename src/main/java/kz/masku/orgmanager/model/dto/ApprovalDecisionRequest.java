package kz.masku.orgmanager.model.dto;

import jakarta.validation.constraints.NotNull;
import kz.masku.orgmanager.model.enums.ApprovalDecision;

/** Body for POST /api/documents/{id}/approvals/{approvalId}/decide. */
public record ApprovalDecisionRequest(

        @NotNull(message = "Decision must be APPROVED or REJECTED")
        ApprovalDecision decision,

        String comment
) {}
