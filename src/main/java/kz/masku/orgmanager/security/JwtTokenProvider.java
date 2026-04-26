package kz.masku.orgmanager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Stateless JWT utility: generates tokens on login and validates them on each request.
 * Uses HMAC-SHA256 (HS256). The secret is loaded from {@code app.jwt.secret} (Base64).
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        // Decode the Base64 secret and derive the HMAC key
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Generates a signed JWT token for the authenticated user.
     *
     * @param userDetails Spring Security user details (email used as subject)
     * @return compact JWT string
     */
    public String generateToken(UserDetails userDetails) {
        Date now    = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Extracts the subject (email) from a valid JWT token.
     *
     * @param token JWT string
     * @return email stored as the JWT subject
     */
    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Returns {@code true} if the token is well-formed, correctly signed, and not expired.
     *
     * @param token JWT string to validate
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT token: {}", ex.getMessage());
            return false;
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
