package com.example.product_service.service;

import com.example.product_service.model.Product;
import com.example.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product>getAllProducts(){
        return productRepository.findAll();
    }

    public Optional<Product> getProductByName(String name){
        return productRepository.findByName(name);
    }

    public Optional<Product> getProductById(Long id){
        return productRepository.findById(id);
    }

    public Product createProduct(Product product){
        product.setCreatedDateTime(LocalDateTime.now());
        product.setUpdatedDateTime(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Optional<Product> updateProduct(Long id, Product productDetails) {

        return productRepository.findById(id).map(existingProduct -> {
            existingProduct.setName(productDetails.getName());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setQuantity(productDetails.getQuantity());
            existingProduct.setUpdatedDateTime(LocalDateTime.now());
            return productRepository.save(existingProduct);
        });
    }

    public void deleteProduct(Long id){
        productRepository.deleteById(id);
    }
}
