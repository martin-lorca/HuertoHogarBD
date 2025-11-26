package com.example.demo.service;

import com.example.demo.model.CartItem;
import com.example.demo.model.Product;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository; // Necesitamos el repositorio de Product para obtener detalles

    // 1. Obtener todos los ítems del carrito
    public List<CartItem> getCartItems() {
        return cartItemRepository.findAll();
    }

    // 2. Añadir o Actualizar un Producto en el carrito
    public CartItem addOrUpdateItem(Long productId, int quantity) throws Exception {

        // --- Lógica de Negocio 1: Verificar si el producto existe ---
        Optional<Product> productOpt = productRepository.findById(productId);
        if (!productOpt.isPresent()) {
            throw new Exception("Producto no encontrado con ID: " + productId);
        }
        Product product = productOpt.get();

        // --- Lógica de Negocio 2: Verificar si el ítem ya está en el carrito ---
        CartItem existingItem = cartItemRepository.findByProductId(productId);

        if (existingItem != null) {
            // El producto ya existe: Actualizamos la cantidad
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity <= 0) {
                // Si la cantidad es cero o menos, eliminamos el ítem
                cartItemRepository.delete(existingItem);
                return null;
            }
            existingItem.setQuantity(newQuantity);
            return cartItemRepository.save(existingItem);

        } else if (quantity > 0) {
            // El producto es nuevo y la cantidad es positiva: Creamos un nuevo ítem
            CartItem newItem = new CartItem(
                    productId,
                    product.getName(),
                    product.getPrice(),
                    quantity
            );
            return cartItemRepository.save(newItem);
        }

        return null; // No se hace nada si se intenta añadir 0 o menos de un producto nuevo
    }

    // 3. Eliminar un ítem por completo del carrito
    public void removeItem(Long id) {
        cartItemRepository.deleteById(id);
    }

    // 4. Vaciar todo el carrito
    public void clearCart() {
        cartItemRepository.deleteAll();
    }

    // 5. Opcional: Calcular el total
    public double getTotalPrice() {
        return cartItemRepository.findAll().stream()
                .mapToDouble(item -> item.getUnitPrice() * item.getQuantity())
                .sum();
    }
}
