package kz.masku.orgmanager.model.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** Transitions a DRAFT document to PENDING and assigns the given approvers. */
public record SubmitForApprovalRequest(

        @NotEmpty(message = "At least one approver must be specified")
        List<Long> approverIds
) {}
