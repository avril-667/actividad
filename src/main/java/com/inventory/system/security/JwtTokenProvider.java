package com.inventory.system.security;

import com.inventory.system.entity.Usuario;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Proveedor de tokens JWT para autenticación stateless.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpirationMs;

    private SecretKey key;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Genera access token para un usuario.
     */
    public String generateAccessToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        claims.put("especialidadId", usuario.getEspecialidad() != null ? usuario.getEspecialidad().getId() : null);
        claims.put("nombreCompleto", usuario.getNombreCompleto());
        return createToken(claims, usuario.getEmail(), jwtExpirationMs);
    }

    /**
     * Genera refresh token para un usuario.
     */
    public String generateRefreshToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, usuario.getEmail(), refreshExpirationMs);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(key)
            .compact();
    }

    /**
     * Extrae el email del token.
     */
    public String getEmailFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token.
     */
    public Date getExpirationFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    /**
     * Verifica si el token ha expirado.
     */
    public boolean isTokenExpired(String token) {
        return getExpirationFromToken(token).before(new Date());
    }

    /**
     * Valida el token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Valida el token contra UserDetails (para Spring Security).
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String email = getEmailFromToken(token);
        return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public long getAccessTokenExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshExpirationMs;
    }
}