package com.example.demo.config;

import com.example.demo.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Configuration: Configuración de Spring
@Configuration
// @EnableWebSecurity: Habilita la seguridad web de Spring
@EnableWebSecurity
public class WebSecurityConfig {

    // Necesitamos este servicio para cargar el usuario desde la BD
    @Autowired
    private UserDetailsService userDetailsService;

    // Inyectamos el filtro de JWT que crearemos en el próximo paso
    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // Define el codificador de contraseñas (BCrypt)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Expone el AuthenticationManager (necesario para el login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Configuración de la cadena de filtros de seguridad
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (necesario para REST APIs)
                .csrf(csrf -> csrf.disable())

                // 2. Definir las políticas de autorización (qué rutas son públicas/privadas)
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (Registro y Login)
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()
                        // Endpoints públicos para productos (cualquiera puede ver los productos)
                        .requestMatchers("/api/products", "/api/products/**").permitAll()
                        // Endpoints públicos para el carrito (cualquiera puede interactuar con su carrito)
                        .requestMatchers("/api/cart", "/api/cart/**").permitAll()
                        // Cualquier otra petición requiere autenticación (JWT)
                        .anyRequest().authenticated()
                )

                // 3. Configuración de sesión: Sin estado (STATELESS) para usar JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. Agregar el filtro de JWT antes del filtro estándar de Spring Security
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}