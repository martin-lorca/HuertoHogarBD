package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Se usará para cifrar contraseñas

    @Autowired
    private AuthenticationManager authenticationManager; // Gestiona el login

    @Autowired
    private JwtTokenUtil jwtTokenUtil; // Genera el token


    // --- 1. REGISTRO DE USUARIO ---
    public User registerUser(String username, String password, String fullName) throws Exception {

        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(username)) {
            throw new Exception("El email ya está registrado.");
        }

        // Cifrar la contraseña
        String encodedPassword = passwordEncoder.encode(password);

        // Crear el nuevo usuario con rol por defecto (ROLE_USER)
        User newUser = new User(username, encodedPassword, fullName, List.of("ROLE_USER"));

        return userRepository.save(newUser);
    }

    // --- 2. INICIO DE SESIÓN ---
    public String login(String username, String password) throws Exception {

        // 1. Autenticar las credenciales
        // Si las credenciales son incorrectas, lanza una excepción de autenticación
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // 2. Obtener los detalles del usuario autenticado
        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generar el JWT
        final String token = jwtTokenUtil.generateToken(userDetails);

        return token; // Devolvemos el token que React guardará
    }
}