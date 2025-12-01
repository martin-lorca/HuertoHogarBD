package com.example.demo.security; // Ajusta el paquete si es necesario

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    // La clave secreta debe ser fuerte y almacenada de forma segura (por ejemplo, en application.properties).
    // Nota: El valor por defecto "tuclavesecreta..." es solo para desarrollo. ¡Cámbialo!
    @Value("${jwt.secret:tuclavesecretadeproducciondebescambiarlaahora}")
    private String secret;

    // Tiempo de validez del token: 10 horas
    public static final long JWT_TOKEN_VALIDITY = 10 * 60 * 60; // en segundos

    // --- MÉTODOS DE LECTURA Y EXTRACCIÓN (Parsing) ---

    /**
     * Recupera el nombre de usuario (Subject) del token JWT.
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * Recupera la fecha de expiración del token JWT.
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * Resuelve un 'claim' específico (ej. Subject, Expiration) del token.
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Obtiene todos los Claims (cuerpo) del token después de validarlo con la clave.
     */
    private Claims getAllClaimsFromToken(String token) {
        // 🥇 Opción 1: La mejor práctica, usando la sintaxis moderna (0.11.0+)
        return Jwts.parser()
                .setSigningKey(signingKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- MÉTODOS DE VALIDACIÓN ---

    /**
     * Verifica si el token ha expirado.
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * Valida el token contra los detalles del usuario.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // --- MÉTODOS DE GENERACIÓN (Building) ---

    /**
     * Genera el token JWT a partir de los detalles del usuario.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Puedes añadir información adicional (ej. roles, ID) a los claims
        claims.put("roles", userDetails.getAuthorities());
        return doGenerateToken(claims, userDetails.getUsername());
    }

    /**
     * Creación del token: Define expiración, firma y sujeto (username).
     */
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        // Usa Jwts.builder() para construir el token
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // 'subject' es el username/email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000))
                .signWith(signingKey(), SignatureAlgorithm.HS256) // Firma con clave y algoritmo
                .compact(); // Finaliza la creación y serializa a String
    }

    // --- GESTIÓN DE CLAVES ---

    /**
     * Decodifica la clave secreta base64 para su uso en la firma/verificación.
     */
    private Key signingKey() {
        // Decodifica la clave secreta que está en Base64
        byte[] keyBytes = Decoders.BASE64.decode(this.secret);
        // Crea una clave HMAC ShaKey para la firma
        return Keys.hmacShaKeyFor(keyBytes);
    }
}