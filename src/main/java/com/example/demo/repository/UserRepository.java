package com.example.demo.repository;


import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Mtodo personalizado crucial para el login: buscar por nombre de usuario (email)
    Optional<User> findByUsername(String username);

    // Verificar si ya existe un usuario con ese nombre (para el registro)
    boolean existsByUsername(String username);
}