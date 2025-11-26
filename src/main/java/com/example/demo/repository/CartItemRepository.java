package com.example.demo.repository;

import com.example.demo.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

// Extiende JpaRepository para obtener las operaciones CRUD básicas para CartItem
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Este método es crucial para el carrito: si un producto ya está,
    // queremos encontrarlo por su ID de Producto para actualizar la cantidad, en lugar de añadir uno nuevo.
    CartItem findByProductId(Long productId);

}
