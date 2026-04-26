package kz.masku.orgmanager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.masku.orgmanager.model.dto.LoginRequest;
import kz.masku.orgmanager.model.dto.LoginResponse;
import kz.masku.orgmanager.model.entity.User;
import kz.masku.orgmanager.repository.UserRepository;
import kz.masku.orgmanager.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoint — publicly accessible (no JWT required).
 * On success returns a signed JWT to be sent in subsequent requests
 * as {@code Authorization: Bearer <token>}.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and token issuance")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider      tokenProvider;
    private final UserRepository        userRepository;

    /**
     * Authenticates the user with email and password, returns a JWT.
     *
     * @param request login credentials
     * @return JWT token + user metadata
     */
    @PostMapping("/login")
    @Operation(summary = "Log in and receive a JWT token")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        // Throws AuthenticationException (→ 401) if credentials are wrong
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String token = tokenProvider.generateToken(userDetails);

        User user = userRepository.findByEmail(request.email()).orElseThrow();

        return ResponseEntity.ok(
                new LoginResponse(token, user.getRole().name(), user.getFullName()));
    }
}
