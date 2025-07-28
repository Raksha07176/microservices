package com.example.user_backend.controller;

import com.example.user_backend.model.User;
import com.example.user_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<User> getAllUsers() {
        logger.info("Received request to get all users");
        List<User> users = userRepository.findAll();
        logger.debug("Fetched {} users from database", users.size());
        return users;
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<User> getUserByName(@PathVariable String name) {
        logger.info("Received request to get user by name: {}", name);
        return userRepository.findByNameIgnoreCase(name)
                .map(user -> {
                    logger.debug("User found: {}", user);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    logger.warn("User with name '{}' not found", name);
                    return ResponseEntity.notFound().build();
                });
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        logger.info("Received request to add new user: {}", user);
        User savedUser = userRepository.save(user);
        logger.debug("User saved: {}", savedUser);
        return ResponseEntity.ok(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user){
        logger.info("Received info to update user: {}", id );
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setName(user.getName());
            existingUser.setEmail(user.getEmail());

            User updatedUser = userRepository.save(existingUser);
            logger.debug("User with id {} updated successfully: {}", id, updatedUser);
            return ResponseEntity.ok(updatedUser);
        })
                .orElseGet(() -> {
                    logger.warn("User with id '{}' not found for update", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteUser(@PathVariable Long id){
        logger.info("Received a request to delete an user by id: {}", id);
        if(!userRepository.existsById(id)){
            logger.warn("User does not exist with the id: {}", id);
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        logger.debug("User with id {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }
}
