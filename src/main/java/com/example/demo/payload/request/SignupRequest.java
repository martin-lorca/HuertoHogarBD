package com.example.demo.payload.request;

import java.util.List;

public class SignupRequest {
    private String username;
    private String password;
    private String fullName;
    private List<String> roles; // Opcional, para asignar roles al registrar

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
}