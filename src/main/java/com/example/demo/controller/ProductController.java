package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/products")
@Tag(name = "Productos", description = "Gestión de la información de productos en la tienda.")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Operation(summary = "Obtener todos los productos", description = "Lista todos los productos disponibles. Acceso Público.")
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }

    @Operation(summary = "Obtener un producto por ID", description = "Recupera los detalles de un producto específico usando su ID. Acceso Público.")
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en la base de datos.")
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        return ResponseEntity.ok("El controlador funciona, el acceso es correcto.");
    }

    @Operation(summary = "Actualizar un producto existente", description = "Actualiza la información de un producto por su ID.")
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> existingProduct = productService.findProductById(id);

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            // Actualizamos todos los campos del nuevo modelo
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());
            product.setUnit(productDetails.getUnit());
            product.setImageRes(productDetails.getImageRes());
            product.setRating(productDetails.getRating());
            product.setCategoryId(productDetails.getCategoryId());

            return ResponseEntity.ok(productService.saveProduct(product));
        }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un producto", description = "Elimina un producto por su ID. Solo ADMIN.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}