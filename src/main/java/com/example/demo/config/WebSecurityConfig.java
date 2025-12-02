package com.example.demo.config;

import com.example.demo.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF (necesario para REST APIs)
                .csrf(csrf -> csrf.disable())

                // 2. Definir las políticas de autorización
                .authorizeHttpRequests(auth -> auth
                        // 🎯 2.1. Endpoints públicos (Registro y Login)
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // 🎯 2.2. Endpoints de LECTURA (GET) de Productos: PÚBLICOS
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                        // 🎯 2.3. Endpoints de ESCRITURA (POST, PUT, DELETE) y Carrito: REQUIEREN TOKEN
                        //         La autorización por ROL será manejada por @PreAuthorize en los Controllers.

                        // Rutas de Carrito (asumiendo que POST/GET/DELETE requiere usuario logueado)
                        .requestMatchers("/api/cart", "/api/cart/**").authenticated()

                        // Rutas de Escritura de Productos (POST, PUT, DELETE)
                        // Estas rutas están protegidas por hasRole() vía @PreAuthorize,
                        // pero primero deben pasar la autenticación (Token válido).
                        .requestMatchers(HttpMethod.POST, "/api/products").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                        // 2.4. Cualquier otra petición
                        .anyRequest().authenticated()
                )

                // 3. Configuración de sesión: Sin estado (STATELESS) para usar JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. Agregar el filtro de JWT
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}