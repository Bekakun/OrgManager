package kz.masku.orgmanager.model.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * JSON part of the multipart POST /api/documents request.
 * The file itself is passed as a separate {@code file} part.
 */
public record CreateDocumentRequest(

        @NotBlank
        String title,

        @NotBlank
        String documentType
) {}
