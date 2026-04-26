package kz.masku.orgmanager.model.dto;

import kz.masku.orgmanager.model.entity.Department;

/** Safe projection of a Department entity. */
public record DepartmentResponse(
        Long   id,
        String name,
        Long   headId,
        String headName,
        long   employeeCount
) {
    /** Full constructor — used when employee count is already known. */
    public static DepartmentResponse from(Department dept, long employeeCount) {
        return new DepartmentResponse(
                dept.getId(),
                dept.getName(),
                dept.getHead() != null ? dept.getHead().getId()      : null,
                dept.getHead() != null ? dept.getHead().getFullName() : null,
                employeeCount
        );
    }

    /** Convenience — count unknown (e.g. after save before re-query). */
    public static DepartmentResponse from(Department dept) {
        return from(dept, 0L);
    }
}
