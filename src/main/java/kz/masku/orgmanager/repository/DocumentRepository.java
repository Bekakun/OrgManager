package kz.masku.orgmanager.repository;

import kz.masku.orgmanager.model.entity.Department;
import kz.masku.orgmanager.model.entity.Document;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.DocumentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByAuthor(User author);

    List<Document> findByStatus(DocumentStatus status);

    long countByStatus(DocumentStatus status);

    /** Returns all documents whose author belongs to the given department. */
    @Query("SELECT d FROM Document d WHERE d.author.department = :department")
    List<Document> findByAuthorDepartment(@Param("department") Department department);

    /** Last 5 documents for the dashboard widget, ordered newest first. */
    List<Document> findTop5ByOrderByCreatedAtDesc();

    // ── Paginated queries ─────────────────────────────────────────────────────

    Page<Document> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Document> findByStatusOrderByCreatedAtDesc(DocumentStatus status, Pageable pageable);

    Page<Document> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);

    Page<Document> findByAuthorAndStatusOrderByCreatedAtDesc(User author, DocumentStatus status, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.author.department = :department ORDER BY d.createdAt DESC")
    Page<Document> findByAuthorDepartmentPaged(@Param("department") Department department, Pageable pageable);

    @Query("SELECT d FROM Document d WHERE d.author.department = :department AND d.status = :status ORDER BY d.createdAt DESC")
    Page<Document> findByAuthorDepartmentAndStatusPaged(@Param("department") Department department,
                                                        @Param("status") DocumentStatus status,
                                                        Pageable pageable);
}
