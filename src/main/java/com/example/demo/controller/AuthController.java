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

// Importaciones de Swagger/OpenAPI (SpringDoc)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Endpoints para el registro y el inicio de sesión (login) de usuarios.")
public class AuthController {

    @Autowired
    private AuthService authService;

    // POST /api/auth/register
    @Operation(summary = "Registro de nuevo usuario",
            description = "Registra un nuevo usuario con un nombre de usuario (username), contraseña (password) y nombre completo (fullName).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos necesarios para el registro del usuario (username, password, fullName).",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object"),
                    examples = {
                            @ExampleObject(name = "Solicitud de Registro", value = "{\"username\": \"john.doe\", \"password\": \"securepassword123\", \"fullName\": \"John Doe\"}")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Usuario registrado exitosamente.\", \"username\": \"john.doe\", \"fullName\": \"John Doe\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. campos faltantes o usuario ya existe)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Username y password son requeridos.\"}")))
    })
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
    @Operation(summary = "Inicio de sesión",
            description = "Autentica al usuario usando username y password, devolviendo un JWT (JSON Web Token) si las credenciales son válidas.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales del usuario (username y password).",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(type = "object"),
                    examples = {
                            @ExampleObject(name = "Solicitud de Login", value = "{\"username\": \"john.doe\", \"password\": \"securepassword123\"}")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso. Retorna el JWT.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = JwtResponse.class))), // Suponemos que JwtResponse tiene un campo 'jwt'
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas (Unauthorized)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Credenciales inválidas.\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (ej. campos faltantes)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Username y password son requeridos.\"}")))
    })
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