package kz.masku.orgmanager.model.dto;

import java.util.List;

/**
 * Aggregated view model for the document detail Thymeleaf page.
 * All data is eagerly loaded inside a single transaction to avoid LazyInitializationException.
 */
public record DocumentDetailView(
        DocumentResponse document,
        List<ApprovalResponse> approvals,
        Long myPendingApprovalId,      // non-null when the current user has a pending approval slot
        List<UserResponse> availableApprovers,
        Long currentUserId
) {}
