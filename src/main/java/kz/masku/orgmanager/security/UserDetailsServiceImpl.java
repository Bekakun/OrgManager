package kz.masku.orgmanager.security;

import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Bridges Spring Security's authentication mechanism with our {@link UserRepository}.
 * The email address is used as the "username" throughout the application.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads a user by email. Inactive accounts are rejected at the
     * {@link org.springframework.security.authentication.DaoAuthenticationProvider} level
     * via the {@code enabled} flag.
     *
     * @param email the email address used to log in
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No user found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                // roles() adds the "ROLE_" prefix automatically
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
                .disabled(user.getStatus().name().equals("INACTIVE"))
                .build();
    }
}
