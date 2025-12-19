package com.example.demo.security;

import com.example.demo.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 🎯 MEJORA PASO 2: Bypass para el catálogo de productos
        // Si es un GET a la API de productos, no procesamos nada y seguimos al siguiente filtro
        if ("GET".equalsIgnoreCase(request.getMethod()) && request.getServletPath().startsWith("/api/products")) {
            chain.doFilter(request, response);
            return;
        }

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // 1. SI NO HAY TOKEN: Pasamos al siguiente filtro de inmediato.
        if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer ") || requestTokenHeader.contains("null")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. EXTRAER TOKEN
        jwtToken = requestTokenHeader.substring(7);

        try {
            username = jwtTokenUtil.getUserNameFromJwtToken(jwtToken);
        } catch (IllegalArgumentException e) {
            logger.warn("No se pudo obtener el token JWT, probablemente malformado.");
        } catch (ExpiredJwtException e) {
            logger.warn("El token JWT ha expirado.");
        }

        // 3. VALIDACIÓN
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                if (jwtTokenUtil.validateJwtToken(jwtToken, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            } catch (Exception e) {
                logger.error("Error al autenticar al usuario: " + username);
            }
        }

        // 4. CONTINUAR
        chain.doFilter(request, response);
    }
}