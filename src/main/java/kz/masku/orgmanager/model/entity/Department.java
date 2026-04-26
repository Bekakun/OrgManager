package kz.masku.orgmanager.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an organizational unit (department).
 * head_id is nullable — a department may temporarily have no head.
 */
@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    /** Optional reference to the employee who manages this department. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    private User head;
}
