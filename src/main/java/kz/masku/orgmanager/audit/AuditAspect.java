package kz.masku.orgmanager.audit;

import kz.masku.orgmanager.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * AOP aspect that intercepts methods annotated with {@link Auditable}
 * and writes a record to {@code audit_logs} after successful execution.
 *
 * <p>Fires only on non-exceptional returns; exceptions are NOT logged here
 * (security or domain exceptions propagate normally to the global handler).
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;

    /**
     * Fires after any method annotated with {@link Auditable} returns normally.
     *
     * @param joinPoint metadata about the intercepted method call
     * @param result    the value returned by the method (unused but typed for safety)
     */
    @AfterReturning(
            pointcut = "@annotation(kz.masku.orgmanager.audit.Auditable)",
            returning = "result"
    )
    public void afterReturning(JoinPoint joinPoint, Object result) {
        Auditable auditable = ((MethodSignature) joinPoint.getSignature())
                .getMethod()
                .getAnnotation(Auditable.class);

        String currentUserEmail = resolveCurrentUserEmail();
        String details = "method=" + joinPoint.getSignature().toShortString();

        log.debug("AUDIT fired: action={} entity={} user={}",
                auditable.action(), auditable.entityType(), currentUserEmail);

        auditService.logByEmail(
                currentUserEmail,
                auditable.action(),
                auditable.entityType(),
                null,
                details
        );
    }

    private String resolveCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }
}
