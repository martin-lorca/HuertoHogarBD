package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository; // Importar Repositorio de Usuario
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder; // Importar PasswordEncoder

import java.util.List;

// @Configuration: Indica que esta clase proporciona beans de configuración
@Configuration
public class DataInitializer {

    // @Bean crea una instancia de CommandLineRunner que se ejecuta automáticamente
    @Bean
    public CommandLineRunner loadData(
            ProductRepository productRepository,
            UserRepository userRepository, // Inyectar UserRepository
            PasswordEncoder passwordEncoder // Inyectar PasswordEncoder
    ) {
        return (args) -> {
            System.out.println("Cargando datos iniciales...");

            // --- 1. CARGA DE PRODUCTOS (Se mantiene igual) ---
            if (productRepository.count() == 0) {
                System.out.println("Cargando productos iniciales...");
                productRepository.save(new Product("Manzanas fuji", "Manzanas Fuji crujientes y dulces...", 1200, 150));
                productRepository.save(new Product("Naranjas valencia", "Jugosas y ricas en vitamina C...", 1000, 200));
                productRepository.save(new Product("Plátanos cavendish", "Plátanos maduros y dulces...", 800, 250));
                productRepository.save(new Product("Zanahorias Orgánicas", "Zanahorias crujientes cultivadas sin pesticidas...", 900, 100));
                productRepository.save(new Product("Espinacas frescas", "Espinacas frescas y nutritivas...", 700, 80));
                productRepository.save(new Product("Pimientos tricolor", "Pimientos rojos, amarillos y verdes...", 1500, 120));
                productRepository.save(new Product("Miel orgánica", "Miel pura y orgánica...", 5000, 50));
                productRepository.save(new Product("Quinoa orgánica", "Quinoa orgánica cultivada en los Andes...", 800, 130));
                productRepository.save(new Product("Leche entera", "Leche entera fresca y nutritiva...", 800, 120));
                System.out.println("Productos cargados. Total: " + productRepository.count());
            }

            // --- 2. CREACIÓN DE USUARIO ADMINISTRADOR ---
            final String adminUsername = "admin@huerto.cl";

            if (!userRepository.existsByUsername(adminUsername)) {
                System.out.println("Creando usuario administrador: " + adminUsername);

                // Contraseña sin cifrar: "password123"
                String encodedPassword = passwordEncoder.encode("password123");

                User adminUser = new User(
                        adminUsername,
                        encodedPassword,
                        "Administrador del Huerto",
                        List.of("ROLE_USER", "ROLE_ADMIN") // ¡Le damos ambos roles!
                );

                userRepository.save(adminUser);
                System.out.println("Usuario Admin creado exitosamente.");
            } else {
                System.out.println("El usuario administrador ya existe.");
            }
        };
    }
}