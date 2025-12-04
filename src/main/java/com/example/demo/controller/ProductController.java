package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

// Importaciones de Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/products")
// 1. Anotación a nivel de clase para agrupar y describir el controlador
@Tag(name = "Productos", description = "Gestión de la información de productos en la tienda.")
public class ProductController {

    @Autowired
    private ProductService productService;

    // GET /api/products: Público
    @Operation(summary = "Obtener todos los productos", description = "Lista todos los productos disponibles. Acceso Público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de productos recuperada con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class)))
    })
    @GetMapping
    public List<Product> getAllProducts() {
        return productService.findAllProducts();
    }

    // GET /api/products/{id}: Público
    @Operation(summary = "Obtener un producto por ID", description = "Recupera los detalles de un producto específico usando su ID. Acceso Público.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(
            @Parameter(description = "ID del producto a buscar", required = true) @PathVariable Long id) {
        Optional<Product> product = productService.findProductById(id);
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/products (Crear):
    @Operation(summary = "Crear un nuevo producto", description = "Crea un nuevo producto en la base de datos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto creado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida")
    })
    @PostMapping
    public Product createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Detalles del producto a crear", required = true)
            @RequestBody Product product) {
        return productService.saveProduct(product);
    }

    // PUT /api/products/{id} (Actualizar): REQUIERE ROL ADMIN
    @Operation(summary = "Actualizar un producto existente", description = "Actualiza la información de un producto por su ID. **Requiere ROL ADMIN.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class))),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Falta de rol ADMIN)")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(
            @Parameter(description = "ID del producto a actualizar", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Detalles del producto con las actualizaciones", required = true)
            @RequestBody Product productDetails) {
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

    // DELETE /api/products/{id} (Eliminar): REQUIERE ROL ADMIN
    @Operation(summary = "Eliminar un producto por ID", description = "Elimina un producto por su ID. **Requiere ROL ADMIN.**")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado con éxito (No Content)"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
            @ApiResponse(responseCode = "403", description = "No autorizado (Falta de rol ADMIN)")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "ID del producto a eliminar", required = true) @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}