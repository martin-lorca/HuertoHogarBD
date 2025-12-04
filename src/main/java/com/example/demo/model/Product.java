package com.example.demo.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient; // Importar para la variable no persistente

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Corresponde a val id: Int (usamos Long en JPA para IDs autogenerados)

    private String name; // Corresponde a val name: String
    private int price; // Corresponde a val price: Int
    private String unit; // Nuevo campo: val unit: String
    private int stock; // Corresponde a val stock: Int
    private int imageRes; // Corresponde a val imageRes: Int
    private String description; // Corresponde a val description: String (Tu modelo original tenía este duplicado, lo dejamos una vez)
    private Double rating; // Corresponde a val rating: Double
    private String categoryId;
    @Transient
    private String imageUrl;
    // Corresponde a val categoryId: String (Llave foránea para la categoría)

    @Transient // Indica a JPA que ignore este campo en la base de datos (se usa solo en la lógica de la aplicación)
    private int cantidad = 0; // Corresponde a var cantidad: Int = 0 (usamos 'var' en Kotlin, que implica que es mutable)


    // Constructor vacío (obligatorio para JPA/Hibernate)
    public Product() {
    }

    // Constructor con todos los campos obligatorios
    public Product(String name, int price, String unit, int stock, int imageRes, String description, Double rating, String categoryId) {
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // --- Nuevos Getters/Setters ---

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getImageRes() {
        return imageRes;
    }

    public void setImageRes(int imageRes) {
        this.imageRes = imageRes;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    // --- Getters/Setters existentes corregidos ---

    public int getPrice() { // El tipo de retorno es 'int'
        return price;
    }

    public void setPrice(int price) { // El parámetro es 'int'
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}