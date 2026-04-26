package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.model.dto.DashboardStats;
import kz.masku.orgmanager.model.dto.DocumentResponse;
import kz.masku.orgmanager.model.enums.DocumentStatus;
import kz.masku.orgmanager.model.enums.UserStatus;
import kz.masku.orgmanager.repository.DocumentRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WebDashboardController {

    private final UserRepository     userRepository;
    private final DocumentRepository documentRepository;

    @GetMapping("/dashboard")
    @Transactional(readOnly = true)
    public String dashboard(Model model, Authentication auth) {
        DashboardStats stats = new DashboardStats(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                documentRepository.count(),
                documentRepository.countByStatus(DocumentStatus.PENDING),
                documentRepository.countByStatus(DocumentStatus.APPROVED),
                documentRepository.countByStatus(DocumentStatus.REJECTED)
        );

        List<DocumentResponse> recent = documentRepository.findTop5ByOrderByCreatedAtDesc()
                .stream().map(DocumentResponse::from).toList();

        model.addAttribute("stats", stats);
        model.addAttribute("recentDocuments", recent);
        return "dashboard/index";
    }
}
