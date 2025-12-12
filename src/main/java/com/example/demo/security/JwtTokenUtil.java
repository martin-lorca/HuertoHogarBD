package com.example.demo.security;

import com.example.demo.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

    // La clave secreta (desde application.properties: jwt.secret)
    @Value("${jwt.secret}")
    private String jwtSecret;

    // El tiempo de expiración en milisegundos (desde application.properties: jwt.expiration.ms)
    @Value("${jwt.expiration.ms}")
    private int jwtExpirationMs;

    // --- MÉTODOS DE LECTURA Y EXTRACCIÓN (Parsing) ---

    /**
     * Recupera el nombre de usuario (Subject) del token JWT.
     */
    public String getUserNameFromJwtToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
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
        // <--- AJUSTE CLAVE: Usamos la sintaxis antigua compatible con versiones anteriores de JJWT
        return Jwts.parser()
                .setSigningKey(key()) // Usamos el método key() para obtener la clave
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // --- MÉTODOS DE VALIDACIÓN ---

    /**
     * Verifica si el token ha expirado.
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getClaimFromToken(token, Claims::getExpiration);
        return expiration.before(new Date());
    }

    /**
     * Valida el token contra los detalles del usuario.
     */
    public Boolean validateJwtToken(String token, UserDetails userDetails) {
        try {
            final String username = getUserNameFromJwtToken(token);
            // Si el token es parsable, no ha expirado y el username coincide
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (Exception e) {
            // Manejo genérico de errores de parsing/validación
            return false;
        }
    }

    // --- MÉTODOS DE GENERACIÓN (Building) ---

    /**
     * Genera el token JWT a partir de la autenticación de Spring Security.
     */
    public String generateJwtToken(Authentication authentication) {
        // Tu entidad User es el Principal aquí
        User userPrincipal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject((userPrincipal.getUsername()))
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Usa la propiedad de application.properties
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- GESTIÓN DE CLAVES ---

    /**
     * Decodifica la clave secreta base64 para su uso en la firma/verificación.
     */
    private Key key() {
        // Usamos jwtSecret, el nombre de campo ajustado
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}