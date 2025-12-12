package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    // Inyección por constructor (preferido)
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
    }


    // --- 1. REGISTRO DE USUARIO (Ajustado para aceptar roles y usar RuntimeException) ---
    public User registerUser(String username, String password, String fullName, List<String> roles) {

        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(username)) {
            // Usamos RuntimeException, que es más común en servicios de Spring para errores de negocio
            throw new RuntimeException("El nombre de usuario " + username + " ya está en uso.");
        }

        // Cifrar la contraseña
        String encodedPassword = passwordEncoder.encode(password);

        // Si la lista de roles es nula o vacía, asigna ROLE_USER por defecto
        List<String> finalRoles = (roles == null || roles.isEmpty()) ? List.of("ROLE_USER") : roles;

        // Crear el nuevo usuario
        User newUser = new User(username, encodedPassword, fullName, finalRoles);

        return userRepository.save(newUser);
    }

    // --- 2. INICIO DE SESIÓN (Ajustado para usar el método correcto de JwtTokenUtil) ---
    public String login(String username, String password) {

        // 1. Autenticar las credenciales. Si falla, AuthenticationManager lanza una excepción.
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. Generar el JWT usando el objeto Authentication
        // Usamos el método que definimos: generateJwtToken(Authentication)
        final String token = jwtTokenUtil.generateJwtToken(authentication);

        return token;
    }
}