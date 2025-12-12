package com.example.demo.config;

import com.example.demo.security.JwtRequestFilter;
import com.example.demo.service.UserDetailsServiceImpl; // <--- Importamos tu servicio específico
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider; // <--- Importación clave
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // <--- Nueva: para usar @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // <--- NUEVO: Habilita el uso de @PreAuthorize en Controllers
public class WebSecurityConfig {

    // Cambiamos la inyección al tipo de tu implementación para ser más específico
    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // Definimos las rutas de Swagger/OpenAPI que deben ser públicas.
    private static final String[] SWAGGER_WHITELIST = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html"
    };


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- NUEVOS BEANS PARA EL PROVEEDOR DE AUTENTICACIÓN ---

    @Bean
    public DaoAuthenticationProvider authenticationProvider() { // <-- Configura el proveedor (UserService + Encoder)
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // --- FIN DE NUEVOS BEANS ---


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF
                .csrf(csrf -> csrf.disable())

                // 2. Definir las políticas de autorización
                .authorizeHttpRequests(auth -> auth
                        // 🎯 Rutas de Swagger/OpenAPI
                        .requestMatchers(SWAGGER_WHITELIST).permitAll()

                        // 🎯 Endpoints públicos (Registro y Login)
                        .requestMatchers("/api/auth/register", "/api/auth/signin").permitAll() // <--- Cambié login a signin para un estándar más común

                        // 🎯 Endpoints de LECTURA (GET) de Productos: PÚBLICOS
                        .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").permitAll()

                        // 🎯 Endpoints que REQUIEREN TOKEN (Autenticación)
                        .requestMatchers("/api/cart", "/api/cart/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/products").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                        // 2.4. Cualquier otra petición
                        .anyRequest().authenticated()
                )

                // 3. Configuración de sesión: Sin estado (STATELESS) para usar JWT
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 4. Establecer el Authentication Provider
        http.authenticationProvider(authenticationProvider()); // <--- AÑADIDO: Integra el proveedor

        // 5. Agregar el filtro de JWT
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}