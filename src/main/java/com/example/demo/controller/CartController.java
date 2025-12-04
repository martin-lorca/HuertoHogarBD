package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Importaciones de Swagger/OpenAPI (SpringDoc)
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
// Ruta base para este controlador
@RequestMapping("/api/cart")
// 1. Anotación a nivel de clase para agrupar y describir el controlador
@Tag(name = "Carrito de Compras", description = "Gestión de los ítems y el estado del carrito de compras de la sesión.")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/cart: Obtener todos los ítems del carrito
    @Operation(summary = "Obtener el contenido del carrito",
            description = "Devuelve la lista actual de ítems en el carrito de compras.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de ítems del carrito recuperada con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartItem.class)))
    })
    @GetMapping
    public List<CartItem> getCart() {
        return cartService.getCartItems();
    }

    // GET /api/cart/total: Obtener el precio total del carrito
    @Operation(summary = "Obtener el precio total del carrito",
            description = "Calcula y devuelve el precio total sumado de todos los productos en el carrito.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Total calculado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Double.class)))
    })
    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal() {
        double total = cartService.getTotalPrice();
        return ResponseEntity.ok(total);
    }

    // POST /api/cart/add: Añadir un producto o actualizar la cantidad
    @Operation(summary = "Añadir/Actualizar ítem en el carrito",
            description = "Añade un nuevo producto al carrito o ajusta la cantidad si ya existe. Permite reducir la cantidad con un valor negativo.")
    // Documentación del Request Body (Payload) con Swagger/OpenAPI
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "ID del producto y cantidad a añadir/ajustar.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    // Indicamos que el cuerpo es un objeto Map y proporcionamos ejemplos claros
                    schema = @Schema(type = "object"),
                    examples = {
                            @ExampleObject(name = "Añadir/Aumentar Cantidad", value = "{\"productId\": 1, \"quantity\": 3}"),
                            @ExampleObject(name = "Reducir Cantidad (Ej: -1)", value = "{\"productId\": 1, \"quantity\": -1}")
                    }
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ítem añadido/actualizado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CartItem.class))),
            @ApiResponse(responseCode = "200", description = "Cantidad reducida o ítem eliminado (si la cantidad llegó a cero)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"message\": \"Item quantity reduced, or item removed if quantity reached zero.\"}"))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida o producto no encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{\"error\": \"Product with ID 1 not found.\"}")))
    })
    @PostMapping("/add")
    public ResponseEntity<?> addItemToCart(@RequestBody Map<String, Object> payload) {
        try {
            Long productId = ((Number) payload.get("productId")).longValue();
            int quantity = (Integer) payload.get("quantity");

            CartItem updatedItem = cartService.addOrUpdateItem(productId, quantity);

            if (updatedItem == null && quantity < 0) {
                // Si updatedItem es null y la cantidad es negativa, es que se eliminó
                return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Item quantity reduced, or item removed if quantity reached zero."));
            }
            if (updatedItem == null && quantity > 0) {
                // No debería suceder si la lógica es correcta
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Could not add item to cart."));
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(updatedItem);
        } catch (Exception e) {
            // Manejo de error si el producto no existe o hay otro problema
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    // DELETE /api/cart/{id}: Eliminar un CartItem específico (ej. si el ID del CartItem es 5)
    @Operation(summary = "Eliminar un ítem del carrito por su ID",
            description = "Elimina un ítem específico del carrito usando el ID del CartItem.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ítem eliminado con éxito (No Content)"),
            @ApiResponse(responseCode = "404", description = "Ítem no encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItemFromCart(
            @Parameter(description = "ID del ítem del carrito a eliminar", required = true) @PathVariable Long id) {
        cartService.removeItem(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    // DELETE /api/cart/clear: Vaciar todo el carrito
    @Operation(summary = "Vaciar todo el carrito",
            description = "Elimina todos los ítems del carrito de compras.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Carrito vaciado con éxito (No Content)")
    })
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}