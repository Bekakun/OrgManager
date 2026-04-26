package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class WebAuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, 50);
        model.addAttribute("logsPage", auditLogRepository.findAllByOrderByCreatedAtDesc(pageable));
        model.addAttribute("currentPage", page);
        return "audit/list";
    }
}
