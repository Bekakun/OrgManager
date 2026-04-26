package kz.masku.orgmanager.service;

import kz.masku.orgmanager.audit.Auditable;
import kz.masku.orgmanager.exception.BusinessException;
import kz.masku.orgmanager.exception.ResourceNotFoundException;
import kz.masku.orgmanager.model.dto.DepartmentResponse;
import kz.masku.orgmanager.model.entity.Department;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.repository.DepartmentRepository;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for the Department module (create / read / update / delete).
 * All write operations require ADMIN role — enforced at controller level.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository       userRepository;

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream()
                .map(d -> DepartmentResponse.from(d,
                        userRepository.countByDepartmentId(d.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAllPaged(Pageable pageable) {
        return departmentRepository.findAll(pageable)
                .map(d -> DepartmentResponse.from(d,
                        userRepository.countByDepartmentId(d.getId())));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        Department dept = findById(id);
        return DepartmentResponse.from(dept,
                userRepository.countByDepartmentId(id));
    }

    // ── Write ─────────────────────────────────────────────────────────────────

    @Auditable(action = "DEPARTMENT_CREATED", entityType = "Department")
    public DepartmentResponse create(String name, Long headId) {
        if (departmentRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BusinessException("Отдел с названием «" + name + "» уже существует");
        }
        Department dept = Department.builder()
                .name(name.trim())
                .head(resolveHead(headId))
                .build();
        return DepartmentResponse.from(departmentRepository.save(dept));
    }

    @Auditable(action = "DEPARTMENT_UPDATED", entityType = "Department")
    public DepartmentResponse update(Long id, String name, Long headId) {
        Department dept = findById(id);
        if (!dept.getName().equalsIgnoreCase(name.trim())
                && departmentRepository.existsByNameIgnoreCase(name.trim())) {
            throw new BusinessException("Отдел с названием «" + name + "» уже существует");
        }
        dept.setName(name.trim());
        dept.setHead(resolveHead(headId));
        return DepartmentResponse.from(departmentRepository.save(dept),
                userRepository.countByDepartmentId(id));
    }

    @Auditable(action = "DEPARTMENT_DELETED", entityType = "Department")
    public void delete(Long id) {
        // DB FK is ON DELETE SET NULL — safe to hard-delete
        departmentRepository.delete(findById(id));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Department findById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
    }

    private User resolveHead(Long headId) {
        if (headId == null) return null;
        return userRepository.findById(headId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + headId));
    }
}
