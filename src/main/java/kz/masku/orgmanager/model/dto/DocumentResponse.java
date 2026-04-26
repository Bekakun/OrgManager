package kz.masku.orgmanager.model.dto;

import kz.masku.orgmanager.model.entity.Document;

import java.time.LocalDateTime;

/** API projection of a {@link Document} entity. */
public record DocumentResponse(

        Long id,
        String title,
        String filePath,
        Long authorId,
        String authorName,
        String documentType,
        Integer version,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /**
     * Converts a managed {@link Document} to this DTO.
     * Must be called inside a transaction (accesses lazy {@code author}).
     */
    public static DocumentResponse from(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getTitle(),
                doc.getFilePath(),
                doc.getAuthor().getId(),
                doc.getAuthor().getFullName(),
                doc.getDocumentType(),
                doc.getVersion(),
                doc.getStatus().name(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
