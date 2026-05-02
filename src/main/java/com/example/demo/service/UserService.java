package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Fonction pour enregistrer un utilisateur
    public User registerUser(User user) {
        return userRepository.save(user);
    }

    // Fonction pour lister tous les utilisateurs
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}