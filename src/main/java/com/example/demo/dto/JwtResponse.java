package com.example.demo.dto;

import java.io.Serializable;
import java.util.List;

// Este objeto define la estructura de la respuesta al cliente (React) después del login
public class JwtResponse implements Serializable {

    private static final long serialVersionUID = -8091879091924046844L;

    // Campos necesarios para la respuesta de login
    private String token; // Cambiado de 'jwttoken' a 'token' para simplificar los getters
    private String type = "Bearer"; // Tipo de token
    private Long id;
    private String username;
    private List<String> roles;

    // 1. Constructor Completo (USADO EN AuthController)
    public JwtResponse(String token, Long id, String username, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    // 2. Constructor Simple (Mantenido por si es necesario, pero no usado en AuthController)
    public JwtResponse(String token) {
        this.token = token;
    }


    // --- Getters y Setters ---

    // Getter para 'token' (antes 'jwttoken')
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    // Getter para 'type'
    public String getTokenType() {
        return type;
    }

    public void setTokenType(String tokenType) {
        this.type = tokenType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}