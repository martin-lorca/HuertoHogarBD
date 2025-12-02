package com.example.demo.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // 1. Intentar extraer el token
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);

            try {
                // 2. Intentar obtener el username del token
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                // Token malformado. Registramos el error y continuamos la cadena SIN detenerla.
                logger.warn("No se pudo obtener el token JWT, probablemente malformado.", e);
            } catch (ExpiredJwtException e) {
                // Token expirado. Registramos el error y continuamos la cadena SIN detenerla.
                logger.warn("El token JWT ha expirado.", e);
            }
        }
        // ⚠️ Si el token es nulo o malformado, 'username' sigue siendo nulo.
        //    Esto permite que la solicitud pase a la cadena de seguridad para que
        //    SecurityConfig decida si la ruta es permitAll() o authenticated().


        // 3. Validar e inyectar el contexto de seguridad si el usuario fue extraído
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validar si el token es válido y corresponde al usuario
            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {

                // Crea el token de autenticación de Spring Security
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establece el usuario en el contexto de seguridad para esta solicitud
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }

        // 4. Continuar la cadena de filtros.
        // Este paso es CLAVE: garantiza que todas las solicitudes (con o sin token)
        // lleguen a la configuración final de Spring Security.
        chain.doFilter(request, response);
    }
}