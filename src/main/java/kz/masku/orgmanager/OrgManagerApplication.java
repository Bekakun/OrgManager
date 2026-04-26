package kz.masku.orgmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the OrgManager application.
 * {@code @EnableAsync} is used so that audit writes can eventually be made
 * non-blocking if wrapped with {@code @Async} in {@link kz.masku.orgmanager.service.AuditService}.
 */
@SpringBootApplication
@EnableAsync
public class OrgManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgManagerApplication.class, args);
    }
}
