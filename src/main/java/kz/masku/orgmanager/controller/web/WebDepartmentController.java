package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.repository.UserRepository;
import kz.masku.orgmanager.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/departments")
@RequiredArgsConstructor
public class WebDepartmentController {

    private final DepartmentService departmentService;
    private final UserRepository    userRepository;

    private static final int PAGE_SIZE = 10;

    // ── List ─────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        var deptsPage = departmentService.getAllPaged(
                PageRequest.of(page, PAGE_SIZE, Sort.by("name").ascending()));
        int totalPages = deptsPage.getTotalPages();
        int startPage  = Math.max(0, page - 2);
        int endPage    = Math.min(totalPages - 1, page + 2);

        model.addAttribute("deptsPage",   deptsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("startPage",   startPage);
        model.addAttribute("endPage",     endPage);
        return "departments/list";
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String newForm(Model model) {
        model.addAttribute("users",    userRepository.findAll());
        model.addAttribute("editMode", false);
        return "departments/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@RequestParam String name,
                         @RequestParam(required = false) Long headId,
                         RedirectAttributes ra) {
        try {
            departmentService.create(name, headId);
            ra.addFlashAttribute("successMessage",
                    "Отдел «" + name + "» успешно создан");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/departments/new";
        }
        return "redirect:/departments";
    }

    // ── Edit ──────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("department", departmentService.getById(id));
        model.addAttribute("users",      userRepository.findAll());
        model.addAttribute("editMode",   true);
        return "departments/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) Long headId,
                         RedirectAttributes ra) {
        try {
            departmentService.update(id, name, headId);
            ra.addFlashAttribute("successMessage", "Отдел обновлён");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/departments";
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            departmentService.delete(id);
            ra.addFlashAttribute("successMessage", "Отдел удалён");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", "Не удалось удалить отдел: " + e.getMessage());
        }
        return "redirect:/departments";
    }
}
