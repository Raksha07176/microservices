package com.example.product_service.controller;

import com.example.product_service.model.Product;
import com.example.product_service.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAllProducts(){
        logger.info("Received request to fetch all products");
        return productService.getAllProducts();
    }

    @GetMapping("/by_name/{name}")
    public ResponseEntity<Product> getProductByName(@PathVariable String name){
        logger.info("Received request to fetch a product by name: {}", name);
        return productService.getProductByName(name).map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id){
        logger.info("Received request to fetch product by id: {}", id);
        return productService.getProductById(id).map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        logger.info("Received a request to create a new product: {}", product);
        Product createdProduct = productService.createProduct(product);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product productDetails) {
        logger.info("Received request to update a product by id: {}", id);
        return productService.updateProduct(id, productDetails)
                .map(updatedProduct -> ResponseEntity.ok(updatedProduct)) // Update hone par 200 OK
                .orElse(ResponseEntity.notFound().build()); // Nahi to 404 Not Found
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.info("Received request delete a product by id: {}", id);
        productService.deleteProduct(id);
        // Delete hone par 204 No Content response bhejenge.
        return ResponseEntity.noContent().build();
    }
}
