package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    // C - Create & U - Update
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // R - Read All
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    // R - Read By ID
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    // D - Delete
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}