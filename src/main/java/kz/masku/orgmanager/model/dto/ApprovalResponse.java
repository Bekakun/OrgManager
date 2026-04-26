package kz.masku.orgmanager.model.dto;

import kz.masku.orgmanager.model.entity.DocumentApproval;

import java.time.LocalDateTime;

/** DTO for a single approval record — safe for Thymeleaf rendering (no lazy proxies). */
public record ApprovalResponse(
        Long id,
        Long approverId,
        String approverName,
        String decision,
        String comment,
        LocalDateTime decidedAt
) {
    public static ApprovalResponse from(DocumentApproval a) {
        return new ApprovalResponse(
                a.getId(),
                a.getApprover().getId(),
                a.getApprover().getFullName(),
                a.getDecision().name(),
                a.getComment(),
                a.getDecidedAt()
        );
    }
}
