package kz.masku.orgmanager.service;

import kz.masku.orgmanager.audit.Auditable;
import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.exception.ResourceNotFoundException;
import kz.masku.orgmanager.model.dto.*;
import kz.masku.orgmanager.model.entity.Document;
import kz.masku.orgmanager.model.entity.DocumentApproval;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.ApprovalDecision;
import kz.masku.orgmanager.model.enums.DocumentStatus;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.repository.DocumentApprovalRepository;
import kz.masku.orgmanager.repository.DocumentRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Document module: CRUD operations and file storage.
 * Status transitions are exclusively handled by {@link WorkflowService}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository         documentRepository;
    private final UserRepository             userRepository;
    private final DocumentApprovalRepository approvalRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    /**
     * Creates a new document in {@link DocumentStatus#DRAFT} status.
     * Optionally stores an attached file on the server filesystem.
     */
    @Auditable(action = "DOCUMENT_CREATED", entityType = "Document")
    public DocumentResponse createDocument(CreateDocumentRequest request,
                                           MultipartFile file,
                                           Authentication authentication) {
        User author   = resolveCurrentUser(authentication);
        String filePath = (file != null && !file.isEmpty()) ? storeFile(file) : null;

        Document document = Document.builder()
                .title(request.title())
                .documentType(request.documentType())
                .filePath(filePath)
                .author(author)
                .status(DocumentStatus.DRAFT)
                .version(1)
                .build();

        return DocumentResponse.from(documentRepository.save(document));
    }

    /**
     * Returns a document DTO enforcing RBAC read rules.
     *
     * @throws AccessDeniedException     if the user may not view this document
     * @throws ResourceNotFoundException if the document does not exist
     */
    @Transactional(readOnly = true)
    public DocumentResponse getDocumentById(Long id, Authentication authentication) {
        Document doc = findById(id);
        checkReadAccess(doc, authentication);
        return DocumentResponse.from(doc);
    }

    /**
     * Returns all documents visible to the current user.
     * ADMIN → all; MANAGER → own department; EMPLOYEE → own documents.
     */
    @Transactional(readOnly = true)
    public List<DocumentResponse> getAllDocuments(Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) {
            return documentRepository.findAll().stream()
                    .map(DocumentResponse::from).toList();
        }
        User user = resolveCurrentUser(authentication);
        if (hasRole(authentication, "ROLE_MANAGER") && user.getDepartment() != null) {
            return documentRepository.findByAuthorDepartment(user.getDepartment()).stream()
                    .map(DocumentResponse::from).toList();
        }
        return documentRepository.findByAuthor(user).stream()
                .map(DocumentResponse::from).toList();
    }

    /**
     * Loads the complete data needed for the document detail Thymeleaf page.
     * All lazy associations are resolved inside this single transaction.
     */
    @Transactional(readOnly = true)
    public DocumentDetailView getDocumentDetailView(Long id, Authentication authentication) {
        Document doc         = findById(id);
        checkReadAccess(doc, authentication);
        User currentUser     = resolveCurrentUser(authentication);

        List<DocumentApproval> approvals = approvalRepository.findByDocument(doc);

        Long myPendingApprovalId = approvals.stream()
                .filter(a -> a.getApprover().getId().equals(currentUser.getId())
                          && a.getDecision() == ApprovalDecision.PENDING)
                .findFirst()
                .map(DocumentApproval::getId)
                .orElse(null);

        List<ApprovalResponse> approvalDtos = approvals.stream()
                .map(ApprovalResponse::from).toList();

        List<UserResponse> availableApprovers = Stream.concat(
                userRepository.findByRole(Role.MANAGER).stream(),
                userRepository.findByRole(Role.ADMIN).stream()
        ).map(UserResponse::from).distinct().toList();

        return new DocumentDetailView(
                DocumentResponse.from(doc),
                approvalDtos,
                myPendingApprovalId,
                availableApprovers,
                currentUser.getId()
        );
    }

    // ── Package-visible helpers used by WorkflowService and web controllers ──

    /** Returns the raw entity or throws {@link ResourceNotFoundException}. */
    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    /** Resolves the currently authenticated user from the database. */
    User resolveCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user not found: " + authentication.getName()));
    }

    /** Checks whether the current authentication carries the given Spring authority. */
    boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    // ── Access control ────────────────────────────────────────────────────────

    void checkReadAccess(Document doc, Authentication authentication) {
        if (hasRole(authentication, "ROLE_ADMIN")) return;

        User user = resolveCurrentUser(authentication);

        if (hasRole(authentication, "ROLE_MANAGER")) {
            boolean sameOrNoDept = doc.getAuthor().getDepartment() == null
                    || user.getDepartment() == null
                    || doc.getAuthor().getDepartment().getId()
                            .equals(user.getDepartment().getId());
            if (sameOrNoDept) return;
        }

        if (!doc.getAuthor().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not have access to document #" + doc.getId());
        }
    }

    // ── File storage ──────────────────────────────────────────────────────────

    private String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Path.of(uploadDir);
            Files.createDirectories(uploadPath);
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
            String filename  = UUID.randomUUID() + "_" + original;
            Path target      = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("File storage failed: " + e.getMessage());
        }
    }
}
