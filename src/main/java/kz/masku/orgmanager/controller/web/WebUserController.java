package kz.masku.orgmanager.controller.web;

import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.model.dto.CreateUserRequest;
import kz.masku.orgmanager.model.dto.UpdateUserRequest;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;
import kz.masku.orgmanager.repository.DepartmentRepository;
import kz.masku.orgmanager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class WebUserController {

    private final UserService          userService;
    private final DepartmentRepository departmentRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Transactional(readOnly = true)
    public String list(@RequestParam(required = false) Long departmentId,
                       @RequestParam(required = false) Role role,
                       Model model) {
        model.addAttribute("users",       userService.getAllUsers(departmentId, role));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles",       Role.values());
        model.addAttribute("selectedDept", departmentId);
        model.addAttribute("selectedRole", role);
        return "users/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String newForm(Model model) {
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles",       Role.values());
        model.addAttribute("editMode",    false);
        return "users/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam String password,
                         @RequestParam(required = false) String position,
                         @RequestParam(required = false) Long departmentId,
                         @RequestParam Role role,
                         RedirectAttributes ra) {
        try {
            userService.createUser(new CreateUserRequest(fullName, email, password, position, departmentId, role));
            ra.addFlashAttribute("successMessage", "Сотрудник «" + fullName + "» успешно создан");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/users/new";
        }
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user",        userService.getUserById(id));
        model.addAttribute("departments", departmentRepository.findAll());
        model.addAttribute("roles",       Role.values());
        model.addAttribute("statuses",    UserStatus.values());
        model.addAttribute("editMode",    true);
        return "users/form";
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String fullName,
                         @RequestParam(required = false) String position,
                         @RequestParam(required = false) Long departmentId,
                         @RequestParam(required = false) Role role,
                         @RequestParam(required = false) UserStatus status,
                         RedirectAttributes ra) {
        try {
            userService.updateUser(id, new UpdateUserRequest(fullName, position, departmentId, role, status));
            ra.addFlashAttribute("successMessage", "Данные сотрудника обновлены");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public String deactivate(@PathVariable Long id, RedirectAttributes ra) {
        try {
            userService.deactivateUser(id);
            ra.addFlashAttribute("successMessage", "Сотрудник деактивирован");
        } catch (BusinessException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/users";
    }
}
