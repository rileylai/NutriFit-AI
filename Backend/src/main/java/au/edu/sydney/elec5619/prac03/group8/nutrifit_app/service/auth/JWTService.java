package au.edu.sydney.elec5619.prac03.group8.nutrifit_app.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(String uuid) {
        log.debug("Generating JWT token for UUID: {}", uuid);
        Map<String, Object> claims = new HashMap<>();
        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(uuid)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .and()
                .signWith(getKey())
                .compact();
        log.debug("JWT token generated successfully for UUID: {}", uuid);
        return token;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String uuid = extractUuid(token);
            boolean isValid = uuid.equals(userDetails.getUsername()) && isTokenExpired(token);
            if (!isValid) {
                log.warn("⚠️ Token validation failed - UUID mismatch or expired token for user: {}", uuid);
            }
            log.debug("Token validation result for UUID {}: {}", uuid, isValid);
            return isValid;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private SecretKey getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimResolver) {
        try {
            final Claims claims = extractAllClaims(token);
            return claimResolver.apply(claims);
        } catch (Exception e) {
            log.error("Failed to extract claim from token: {}", e.getMessage());
            throw e;
        }
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return !extractExpiration(token).before(new Date());
    }


    public String extractUuid(String token) {
        try {
            String uuid = extractClaim(token, Claims::getSubject);
            log.trace("Extracted UUID from token: {}", uuid);
            return uuid;
        } catch (Exception e) {
            log.error("Failed to extract UUID from token: {}", e.getMessage());
            throw e;
        }
    }
}
