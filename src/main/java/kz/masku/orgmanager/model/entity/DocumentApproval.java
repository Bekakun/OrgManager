package kz.masku.orgmanager.model.entity;

import jakarta.persistence.*;
import kz.masku.orgmanager.model.enums.ApprovalDecision;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Tracks a single approver's decision for a given document.
 * One document may have multiple approval records (one per approver).
 * Cascade DELETE is handled at the DB level (ON DELETE CASCADE on document_id FK).
 */
@Entity
@Table(name = "document_approvals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id", nullable = false)
    private User approver;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    @Builder.Default
    private ApprovalDecision decision = ApprovalDecision.PENDING;

    @Column(name = "comment")
    private String comment;

    /** Null until the approver makes a decision. */
    @Column(name = "decided_at")
    private LocalDateTime decidedAt;
}
