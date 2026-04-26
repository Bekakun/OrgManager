package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.model.dto.UserResponse;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects the authenticated user's profile into every Thymeleaf model
 * so the sidebar can display the user's name and role on all pages.
 * Scoped to web controllers only to avoid interfering with REST controllers.
 */
@ControllerAdvice(basePackages = "kz.masku.orgmanager.controller.web")
@RequiredArgsConstructor
public class GlobalModelAttributeAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("currentUser")
    @Transactional(readOnly = true)
    public UserResponse currentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return userRepository.findByEmail(authentication.getName())
                .map(UserResponse::from)
                .orElse(null);
    }
}
