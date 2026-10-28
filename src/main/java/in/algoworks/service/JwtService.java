
package in.algoworks.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.DecodingException;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationHours;

    public JwtService(
            @Value("${jwt.secret:}") String secret,
            @Value("${jwt.expiration-hours:1}") long expirationHours) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET is required. Set it to a Base64-encoded random key of at least 32 bytes.");
        }
        if (expirationHours <= 0) {
            throw new IllegalStateException(
                    "jwt.expiration-hours must be greater than zero.");
        }

        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret.trim());
        } catch (DecodingException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET must be a valid Base64-encoded key of at least 32 bytes.",
                    exception);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must decode to at least 32 bytes for HS256.");
        }

        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expirationHours = expirationHours;
    }

    public String generateToken(String customerId, String email) {
        return generateToken(customerId, email, "CUSTOMER");
    }

    /** Generates a JWT token using the role stored with the authenticated account. */
    public String generateToken(String customerId, String email, String role) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(customerId)
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(
                        now.plus(expirationHours, ChronoUnit.HOURS)
                ))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates a JWT token and returns its claims.
     */
    public Claims validateTokenAndGetClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
