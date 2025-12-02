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

            // --- 1. CARGA DE PRODUCTOS (VALORES ACTUALIZADOS) ---
            if (productRepository.count() == 0) {
                System.out.println("Cargando productos iniciales...");

                // NOTA: Los nuevos parámetros en el constructor son:
                // (name, price, unit, stock, imageRes, description, rating, categoryId)

                productRepository.save(new Product(
                        "Manzanas fuji",
                        1200,
                        "kg",
                        150,
                        101,
                        "Manzanas Fuji crujientes y dulces...",
                        4.8,
                        "FRUTAS"
                ));

                productRepository.save(new Product(
                        "Naranjas valencia",
                        1000,
                        "kg",
                        200,
                        102,
                        "Jugosas y ricas en vitamina C...",
                        4.5,
                        "FRUTAS"
                ));

                productRepository.save(new Product(
                        "Plátanos cavendish",
                        800,
                        "docena",
                        250,
                        103,
                        "Plátanos maduros y dulces...",
                        4.2,
                        "FRUTAS"
                ));

                productRepository.save(new Product(
                        "Zanahorias Orgánicas",
                        900,
                        "manojo",
                        100,
                        104,
                        "Zanahorias crujientes cultivadas sin pesticidas...",
                        4.7,
                        "VEGETALES"
                ));

                productRepository.save(new Product(
                        "Espinacas frescas",
                        700,
                        "bolsa",
                        80,
                        105,
                        "Espinacas frescas y nutritivas...",
                        4.6,
                        "VEGETALES"
                ));

                productRepository.save(new Product(
                        "Pimientos tricolor",
                        1500,
                        "unidad",
                        120,
                        106,
                        "Pimientos rojos, amarillos y verdes...",
                        4.4,
                        "VEGETALES"
                ));

                productRepository.save(new Product(
                        "Miel orgánica",
                        5000,
                        "tarro",
                        50,
                        107,
                        "Miel pura y orgánica...",
                        4.9,
                        "DESPENSA"
                ));

                productRepository.save(new Product(
                        "Quinoa orgánica",
                        800,
                        "bolsa",
                        130,
                        108,
                        "Quinoa orgánica cultivada en los Andes...",
                        4.3,
                        "DESPENSA"
                ));

                productRepository.save(new Product(
                        "Leche entera",
                        800,
                        "litro",
                        120,
                        109,
                        "Leche entera fresca y nutritiva...",
                        4.5,
                        "LACTEOS"
                ));

                System.out.println("Productos cargados. Total: " + productRepository.count());
            }

            // --- 2. CREACIÓN DE USUARIO ADMINISTRADOR (Se mantiene igual) ---
            final String adminUsername = "admin@huerto.cl";

            if (!userRepository.existsByUsername(adminUsername)) {
                System.out.println("Creando usuario administrador: " + adminUsername);

                // Contraseña sin cifrar: "password123"
                String encodedPassword = passwordEncoder.encode("password123");

                User adminUser = new User(
                        adminUsername,
                        encodedPassword,
                        "Administrador del Huerto",
                        List.of("ROLE_USER", "ROLE_ADMIN")
                );

                userRepository.save(adminUser);
                System.out.println("Usuario Admin creado exitosamente.");
            } else {
                System.out.println("El usuario administrador ya existe.");
            }
        };
    }
}