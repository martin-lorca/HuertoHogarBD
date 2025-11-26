package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/products
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // Usa ResponseEntity para manejar la respuesta HTTP (200 OK o 404 Not Found)
        Optional<Product> product = productService.findProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/products (Crear)
    @PostMapping
    public Product createProduct(@RequestBody Product product) {
        // @RequestBody mapea el JSON que viene del React a un objeto Product de Java
        return productService.saveProduct(product);
    }

    // PUT /api/products/{id} (Actualizar)
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        Optional<Product> existingProduct = productService.findProductById(id);

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            // Actualización de campos
            product.setName(productDetails.getName());
            product.setDescription(productDetails.getDescription());
            product.setPrice(productDetails.getPrice());
            product.setStock(productDetails.getStock());

            return ResponseEntity.ok(productService.saveProduct(product));
        }

        return ResponseEntity.notFound().build();
    }

    // DELETE /api/products/{id} (Eliminar)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build(); // Devuelve 204 No Content
    }
}
