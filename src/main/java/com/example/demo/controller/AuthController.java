package com.example.demo.controller;

import com.example.demo.dto.JwtResponse;
import com.example.demo.model.User;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registrationRequest) {
        try {
            String username = registrationRequest.get("username");
            String password = registrationRequest.get("password");
            String fullName = registrationRequest.get("fullName");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos."));
            }

            User user = authService.registerUser(username, password, fullName);

            // Devolvemos solo un mensaje de éxito, la parte de login es separada
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "message", "Usuario registrado exitosamente.",
                    "username", user.getUsername(),
                    "fullName", user.getFullName()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> authenticationRequest) throws Exception {
        try {
            String username = authenticationRequest.get("username");
            String password = authenticationRequest.get("password");

            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos."));
            }

            // Llama al servicio que autentica y genera el token
            final String token = authService.login(username, password);

            // Devuelve el token al frontend
            return ResponseEntity.ok(new JwtResponse(token));
        } catch (Exception e) {
            // Maneja credenciales inválidas (Spring Security lanza BadCredentialsException)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas."));
        }
    }
}
