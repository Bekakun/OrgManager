package kz.masku.orgmanager.repository;

import kz.masku.orgmanager.model.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /** Eagerly loads the user association to avoid LazyInitializationException in Thymeleaf templates. */
    @EntityGraph(attributePaths = {"user"})
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
