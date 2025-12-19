package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner loadData(
            ProductRepository productRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        return (args) -> {
            System.out.println("Cargando datos iniciales...");

            // --- 1. CARGA DE PRODUCTOS ---
            if (productRepository.count() == 0) {
                System.out.println("Cargando productos iniciales...");

                // Mapeo sugerido de IDs:
                // 1L = FRUTAS, 2L = VEGETALES, 3L = DESPENSA, 4L = LACTEOS

                productRepository.save(new Product(
                        "Manzanas fuji", 1200, "kg", 150, 101,
                        "Manzanas Fuji crujientes y dulces...", 4.8, 1L));

                productRepository.save(new Product(
                        "Naranjas valencia", 1000, "kg", 200, 102,
                        "Jugosas y ricas en vitamina C...", 4.5, 1L));

                productRepository.save(new Product(
                        "Plátanos cavendish", 800, "kg", 250, 103,
                        "Plátanos maduros y dulces...", 4.2, 1L));

                productRepository.save(new Product(
                        "Zanahorias Orgánicas", 900, "kg", 100, 104,
                        "Zanahorias crujientes cultivadas sin pesticidas...", 4.7, 2L));

                productRepository.save(new Product(
                        "Espinacas frescas", 700, "bolsa", 80, 105,
                        "Espinacas frescas y nutritivas...", 4.6, 2L));

                productRepository.save(new Product(
                        "Pimientos tricolor", 1500, "kg", 120, 106,
                        "Pimientos rojos, amarillos y verdes...", 4.4, 2L));

                productRepository.save(new Product(
                        "Miel orgánica", 5000, "frasco", 50, 107,
                        "Miel pura y orgánica...", 4.9, 3L));

                productRepository.save(new Product(
                        "Quinoa orgánica", 800, "bolsa", 130, 108,
                        "Quinoa orgánica cultivada en los Andes...", 4.3, 3L));

                productRepository.save(new Product(
                        "Leche entera", 800, "litro", 120, 109,
                        "Leche entera fresca y nutritiva...", 4.5, 4L));

                System.out.println("Productos cargados. Total: " + productRepository.count());
            }

            // --- 2. USUARIOS ---
            final String adminUsername = "admin@huerto.cl";
            if (!userRepository.existsByUsername(adminUsername)) {
                User adminUser = new User(
                        adminUsername,
                        passwordEncoder.encode("password123"),
                        "Administrador del Huerto",
                        List.of("ROLE_USER", "ROLE_ADMIN")
                );
                userRepository.save(adminUser);
            }

            final String userUsername = "user@huerto.cl";
            if (!userRepository.existsByUsername(userUsername)) {
                User regularUser = new User(
                        userUsername,
                        passwordEncoder.encode("password123"),
                        "Usuario Regular de Prueba",
                        List.of("ROLE_USER")
                );
                userRepository.save(regularUser);
            }
        };
    }
}