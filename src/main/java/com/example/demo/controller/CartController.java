package com.example.demo.controller;

import com.example.demo.model.CartItem;
import com.example.demo.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
// Ruta base para este controlador
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET /api/cart: Obtener todos los ítems del carrito
    @GetMapping
    public List<CartItem> getCart() {
        return cartService.getCartItems();
    }

    // GET /api/cart/total: Obtener el precio total del carrito
    @GetMapping("/total")
    public ResponseEntity<Double> getCartTotal() {
        double total = cartService.getTotalPrice();
        return ResponseEntity.ok(total);
    }

    // POST /api/cart/add: Añadir un producto o actualizar la cantidad
    // Espera un JSON como: { "productId": 1, "quantity": 1 }
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
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeItemFromCart(@PathVariable Long id) {
        cartService.removeItem(id);
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }

    // DELETE /api/cart/clear: Vaciar todo el carrito
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        cartService.clearCart();
        return ResponseEntity.noContent().build(); // HTTP 204 No Content
    }
}
