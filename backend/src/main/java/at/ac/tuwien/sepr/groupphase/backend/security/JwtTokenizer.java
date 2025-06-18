package at.ac.tuwien.sepr.groupphase.backend.security;

import at.ac.tuwien.sepr.groupphase.backend.config.properties.SecurityProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class JwtTokenizer {

    private final SecurityProperties securityProperties;

    public JwtTokenizer(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public String getAuthToken(String user, List<String> roles, Optional<String> departmentName) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        SecretKey key = Keys.hmacShaKeyFor(signingKey);

        var builder = Jwts.builder()
            .header().add("typ", securityProperties.getJwtType()).and()
            .issuer(securityProperties.getJwtIssuer())
            .audience().add(securityProperties.getJwtAudience()).and()
            .subject(user)
            .expiration(new Date(System.currentTimeMillis() + securityProperties.getJwtExpirationTime()))
            .claim("rol", roles);

        departmentName.ifPresent(depName -> builder.claim("depName", depName));

        var token = builder.signWith(key, Jwts.SIG.HS512).compact();
        return securityProperties.getAuthTokenPrefix() + token;
    }

    public String generateInvitationToken(String email) {
        byte[] signingKey = securityProperties.getJwtSecret().getBytes();
        SecretKey key = Keys.hmacShaKeyFor(signingKey);

        return Jwts.builder()
            .header().add("typ", "INVITATION").and()
            .issuer(securityProperties.getJwtIssuer())
            .audience().add(securityProperties.getJwtAudience()).and()
            .subject(email)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + 86400000)) // 24 Stunden
            .claim("purpose", "invitation")
            .signWith(key, Jwts.SIG.HS512)
            .compact();
    }
}