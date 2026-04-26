package kz.masku.orgmanager.audit;

import java.lang.annotation.*;

/**
 * Marks a service method for audit logging via {@link AuditAspect}.
 * The aspect fires after the method returns successfully.
 *
 * <pre>{@code
 * @Auditable(action = "DOCUMENT_CREATED", entityType = "Document")
 * public DocumentResponse createDocument(...) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Auditable {

    /** Human-readable action label stored in audit_logs.action. */
    String action();

    /** Entity type label stored in audit_logs.entity_type. */
    String entityType() default "";
}
