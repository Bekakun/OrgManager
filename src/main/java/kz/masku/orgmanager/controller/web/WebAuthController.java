package kz.masku.orgmanager.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebAuthController {

    /** Redirects root URL to the dashboard. */
    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    /** Renders the login page. Spring Security handles the actual POST /login. */
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
}
