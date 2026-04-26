package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.model.dto.ApprovalDecisionRequest;
import kz.masku.orgmanager.model.dto.CreateDocumentRequest;
import kz.masku.orgmanager.model.dto.SubmitForApprovalRequest;
import kz.masku.orgmanager.model.enums.ApprovalDecision;
import kz.masku.orgmanager.service.DocumentService;
import kz.masku.orgmanager.service.WorkflowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/documents")
@RequiredArgsConstructor
public class WebDocumentController {

    private final DocumentService documentService;
    private final WorkflowService workflowService;

    @GetMapping
    public String list(Authentication auth, Model model) {
        model.addAttribute("documents", documentService.getAllDocuments(auth));
        return "documents/list";
    }

    @GetMapping("/new")
    public String newForm() {
        return "documents/form";
    }

    @PostMapping
    public String create(@RequestParam String title,
                         @RequestParam String documentType,
                         @RequestParam(required = false) MultipartFile file,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            documentService.createDocument(new CreateDocumentRequest(title, documentType), file, auth);
            ra.addFlashAttribute("successMessage", "Документ «" + title + "» создан");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/documents/new";
        }
        return "redirect:/documents";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model,
                         RedirectAttributes ra) {
        try {
            model.addAttribute("detail", documentService.getDocumentDetailView(id, auth));
        } catch (AccessDeniedException e) {
            ra.addFlashAttribute("errorMessage", "Нет доступа к этому документу");
            return "redirect:/documents";
        }
        return "documents/detail";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam List<Long> approverIds,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            workflowService.submitForApproval(id, new SubmitForApprovalRequest(approverIds), auth);
            ra.addFlashAttribute("successMessage", "Документ отправлен на согласование");
        } catch (BusinessException | AccessDeniedException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documents/" + id;
    }

    @PostMapping("/{id}/approvals/{approvalId}/decide")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String decide(@PathVariable Long id,
                         @PathVariable Long approvalId,
                         @RequestParam ApprovalDecision decision,
                         @RequestParam(required = false) String comment,
                         Authentication auth,
                         RedirectAttributes ra) {
        try {
            workflowService.processApproval(id, approvalId,
                    new ApprovalDecisionRequest(decision, comment), auth);
            ra.addFlashAttribute("successMessage",
                    decision == ApprovalDecision.APPROVED ? "Документ одобрен" : "Документ отклонён");
        } catch (BusinessException | AccessDeniedException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documents/" + id;
    }

    @PostMapping("/{id}/return-to-draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public String returnToDraft(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        try {
            workflowService.returnToDraft(id, auth);
            ra.addFlashAttribute("successMessage", "Документ возвращён на доработку");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/documents/" + id;
    }
}
