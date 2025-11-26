package com.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Esta entidad representa un ítem específico dentro del carrito de compras.
@jakarta.persistence.Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Usamos el ID del producto y la cantidad, ya que la información del producto
    // (nombre, precio) ya está en la tabla Product.
    // Usamos un simple Long productId para este ejemplo.
    private Long productId;

    private String productName; // Guardamos el nombre para simplificar la respuesta
    private double unitPrice;   // Guardamos el precio unitario en el momento de la adición
    private int quantity;

    // NOTA: En una aplicación real, usaríamos una entidad 'User'
    // y un campo 'userId' para asociar el carrito a un usuario.
    // Para simplificar, asumiremos que todos los ítems son para un carrito único.


    public CartItem() {
    }

    // Constructor completo
    public CartItem(Long productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}