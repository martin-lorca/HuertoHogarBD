package com.example.demo.controller;

import com.example.demo.dto.JwtResponse;
import com.example.demo.model.User;
import com.example.demo.payload.request.LoginRequest;
import com.example.demo.payload.request.SignupRequest;
import com.example.demo.repository.UserRepository; // <<--- NUEVA IMPORTACIÓN
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Nueva importación para manejo de error
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// Importaciones de Swagger/OpenAPI (SpringDoc)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para el registro y el inicio de sesión (signin) de usuarios.")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository; // <<--- NUEVA INYECCIÓN PARA BUSCAR EL USUARIO

    // ... (El método /register se mantiene igual y funciona correctamente)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signUpRequest) {
        try {
            if (signUpRequest.getUsername() == null || signUpRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos."));
            }

            authService.registerUser(
                    signUpRequest.getUsername(),
                    signUpRequest.getPassword(),
                    signUpRequest.getFullName(),
                    signUpRequest.getRoles()
            );

            return ResponseEntity.ok(Map.of("message", "Usuario registrado exitosamente!"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }


    // POST /api/auth/signin
    @Operation(summary = "Inicio de sesión (Signin)",
            description = "Autentica al usuario usando username y password, devolviendo un JWT con los detalles del usuario.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales del usuario (username y password).",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = {
                            @ExampleObject(name = "Solicitud de Login", value = "{\"username\": \"john.doe\", \"password\": \"securepassword123\"}")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso. Retorna el JWT y detalles.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (Unauthorized)")
    })
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username y password son requeridos."));
            }

            // 1. Llama al servicio para autenticar y generar el token.
            // Esto valida el usuario/contraseña y, si es exitoso, genera el JWT.
            String jwt = authService.login(loginRequest.getUsername(), loginRequest.getPassword());

            // 2. Dado que el casting directo falla, buscamos la entidad User completa
            // en la base de datos usando el username que acaba de ser validado.
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado después de la autenticación."));


            // 3. Mapear roles a List<String>. Usa la entidad 'user' obtenida.
            List<String> roles = user.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            // 4. Devolver la respuesta completa (JwtResponse)
            return ResponseEntity.ok(new JwtResponse(jwt,
                    user.getId(), // Usamos la entidad 'user' para obtener el ID
                    user.getUsername(),
                    roles));
        } catch (org.springframework.security.core.AuthenticationException e) {
            // Manejar credenciales inválidas (Invalid username or password)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Credenciales inválidas."));
        } catch (Exception e) {
            // Error inesperado del servidor (incluyendo UsernameNotFoundException si falla la búsqueda)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Ocurrió un error inesperado al iniciar sesión."));
        }
    }
}