package kz.masku.orgmanager.config;

import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.model.enums.Role;
import kz.masku.orgmanager.model.enums.UserStatus;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Runs on every startup to guarantee the default admin account exists
 * and has the correct password hash.
 *
 * This corrects the case where the Flyway seed SQL contained a placeholder
 * BCrypt hash that did not match the actual "admin123" password.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL    = "admin@orgmanager.kz";
    private static final String ADMIN_PASSWORD = "Admin123!";

    @Override
    @Transactional
    public void run(String... args) {
        userRepository.findByEmail(ADMIN_EMAIL).ifPresentOrElse(
            admin -> {
                // The Flyway seed may have inserted a wrong placeholder hash — fix it.
                if (!passwordEncoder.matches(ADMIN_PASSWORD, admin.getPasswordHash())) {
                    admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
                    userRepository.save(admin);
                    log.info("✔ Admin password hash corrected for: {}", ADMIN_EMAIL);
                } else {
                    log.info("✔ Admin account OK: {}", ADMIN_EMAIL);
                }
            },
            () -> {
                // Fresh database — Flyway seed did not run yet or was removed.
                User admin = User.builder()
                        .fullName("System Administrator")
                        .email(ADMIN_EMAIL)
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .position("Administrator")
                        .role(Role.ADMIN)
                        .status(UserStatus.ACTIVE)
                        .build();
                userRepository.save(admin);
                log.info("✔ Default admin account created: {}", ADMIN_EMAIL);
            }
        );
    }
}
