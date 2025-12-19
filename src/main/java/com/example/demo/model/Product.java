package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "products") // Asegura que mapee a la tabla correcta
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int price;
    private String unit;
    private int stock;
    private int imageRes;
    private String description;
    private Double rating;

    // Cambiado a Long para consistencia con IDs de BD,
    // y para evitar errores de tipo en la búsqueda.
    private Long categoryId;

    @Transient
    private String imageUrl;

    @Transient
    private int cantidad = 0;

    // Constructor vacío obligatorio para JPA
    public Product() {
    }

    // Constructor completo (Actualizado con Long categoryId)
    public Product(String name, int price, String unit, int stock, int imageRes, String description, Double rating, Long categoryId) {
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.stock = stock;
        this.imageRes = imageRes;
        this.description = description;
        this.rating = rating;
        this.categoryId = categoryId;
    }

    // =========================================================================
    // Getters y Setters
    // =========================================================================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public int getImageRes() { return imageRes; }
    public void setImageRes(int imageRes) { this.imageRes = imageRes; }

    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}