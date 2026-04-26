package kz.masku.orgmanager.repository;

import kz.masku.orgmanager.model.entity.Document;
import kz.masku.orgmanager.model.entity.DocumentApproval;
import kz.masku.orgmanager.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentApprovalRepository extends JpaRepository<DocumentApproval, Long> {

    List<DocumentApproval> findByDocument(Document document);

    List<DocumentApproval> findByApprover(User approver);

    /** Used by WorkflowService when a REJECTED document is returned to DRAFT. */
    void deleteByDocument(Document document);
}
