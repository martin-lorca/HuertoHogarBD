package com.example.demo.config;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// @Configuration: Indica que esta clase proporciona beans de configuración
@Configuration
public class DataInitializer {

    // @Bean crea una instancia de CommandLineRunner que se ejecuta automáticamente
    // tan pronto como la aplicación Spring Boot se ha iniciado completamente.
    @Bean
    public CommandLineRunner loadData(ProductRepository repository) {
        return (args) -> {
            System.out.println("Cargando datos iniciales de productos...");

            // Creamos y guardamos productos de ejemplo para el frontend
            repository.save(new Product("Manzanas fuji", "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule. Perfectas para meriendas saludables o como ingrediente en postres. Estas manzanas son conocidas por su textura firme y su sabor equilibrado entre dulce y ácido..", 1200, 150));
            repository.save(new Product("Naranjas valencia", "Jugosas y ricas en vitamina C, estas naranjas Valencia son ideales para zumos frescos y refrescantes. Cultivadas en condiciones climáticas óptimas que aseguran su dulzura y jugosidad..", 1000, 200));
            repository.save(new Product("Plátanos cavendish", "Plátanos maduros y dulces, perfectos para el desayuno o como snack energético. Estos plátanos son ricos en potasio y vitaminas, ideales para mantener una dieta equilibrada.", 800, 250));
            repository.save(new Product("Zanahorias Orgánicas", "Zanahorias crujientes cultivadas sin pesticidas en la Región de O'Higgins. Excelente fuente de vitamina A y fibra, ideales para ensaladas, jugos o como snack saludable.", 900, 100));
            repository.save(new Product("Espinacas frescas", "Espinacas frescas y nutritivas, perfectas para ensaladas y batidos verdes. Estas espinacas son cultivadas bajo prácticas orgánicas que garantizan su calidad y valor nutricional", 700, 80));
            repository.save(new Product("Pimientos tricolor", "Pimientos rojos, amarillos y verdes, ideales para salteados y platos coloridos. Ricos en antioxidantes y vitaminas, estos pimientos añaden un toque vibrante y saludable a cualquier receta.", 1500, 120));
            repository.save(new Product("Miel orgánica", "Miel pura y orgánica, rica en antioxidantes, perfecta para endulzar naturalmente.", 5000, 50));
            repository.save(new Product("Quinoa orgánica", "Quinoa orgánica cultivada en los Andes, conocida como un superalimento por su alto contenido en proteínas, fibra y minerales. Es libre de gluten y versátil en la cocina, perfecta para ensaladas, guisos y platos saludables.", 800, 130));
            repository.save(new Product("Leche entera", "Leche entera fresca y nutritiva, proveniente de ganado local. Rica en calcio, proteínas y vitaminas esenciales, ideal para fortalecer los huesos y mantener una alimentación equilibrada. Perfecta para desayunos, batidos y recetas caseras.", 800, 120));



            System.out.println("Productos cargados. Total: " + repository.count());

            // Opcional: Imprimir los datos para verificar
            // repository.findAll().forEach(p -> System.out.println("ID: " + p.getId() + ", Nombre: " + p.getName()));
        };
    }
}