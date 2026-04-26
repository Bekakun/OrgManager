package kz.masku.orgmanager.repository;

import kz.masku.orgmanager.model.entity.Department;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findByDepartment(Department department);

    List<User> findByDepartmentId(Long departmentId);

    List<User> findByRole(Role role);

    List<User> findByDepartmentAndRole(Department department, Role role);

    boolean existsByEmail(String email);

    long countByStatus(UserStatus status);
}
