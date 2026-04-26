package kz.masku.orgmanager.model.entity;

import jakarta.persistence.*;
import kz.masku.orgmanager.model.enums.DocumentStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Electronic document subject to the approval workflow.
 * Status transitions are managed exclusively by {@link kz.masku.orgmanager.service.WorkflowService}.
 */
@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    /** Server-side path to the uploaded file, nullable until a file is attached. */
    @Column(name = "file_path")
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "document_type", nullable = false)
    private String documentType;

    /** Incremented by WorkflowService each time a REJECTED doc is returned to DRAFT. */
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.DRAFT;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt  = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
