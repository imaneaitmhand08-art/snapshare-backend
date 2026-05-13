package com.example.demo.service;

import com.example.demo.model.Follow;
import com.example.demo.model.User;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

/**
 * AVEC SPRING : @Service + @Autowired = injection de dépendances automatique
 * SANS SPRING : instanciation manuelle → new UserRepository()
 * + gestion manuelle des transactions JDBC
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Inscription d'un nouvel utilisateur
     * AVEC SPRING SECURITY : passwordEncoder.encode() hache le mot de passe automatiquement
     * SANS SPRING : implémenter SHA-256 manuellement ou stocker en clair (dangereux)
     */
    public User registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email déjà utilisé");
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Nom d'utilisateur déjà pris");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Recherche d'utilisateurs par nom
     * AVEC SPRING : searchByUsername() via @Query dans le repository
     * SANS SPRING : "SELECT * FROM users WHERE username LIKE '%?%'" via JDBC
     */
    public List<User> searchUsers(String keyword) {
        return userRepository.searchByUsername(keyword);
    }

    /**
     * Suivre un utilisateur
     * AVEC SPRING : @ManyToMany gère la table de jointure automatiquement
     * SANS SPRING : INSERT INTO follows (follower_id, following_id) VALUES (?, ?)
     */
    public void followUser(User follower, User following) {
        if (!followRepository.existsByFollowerAndFollowing(follower, following)) {
            Follow follow = new Follow();
            follow.setFollower(follower);
            follow.setFollowing(following);
            followRepository.save(follow);
        }
    }

    public void unfollowUser(User follower, User following) {
        followRepository.findByFollowerAndFollowing(follower, following)
            .ifPresent(followRepository::delete);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public User saveUser(User user) {
    return userRepository.save(user);
}
}