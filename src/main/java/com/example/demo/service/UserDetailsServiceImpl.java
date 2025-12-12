package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Importación nueva

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    // Método que Spring Security llama para cargar un usuario por su username (email)
    @Override
    @Transactional // Buena práctica para asegurar que la sesión de JPA esté abierta
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // El mensaje de error fue ajustado para reflejar que buscas por username
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + username);
        }

        // Devolvemos el objeto User (que implementa UserDetails)
        return userOptional.get();
    }
}