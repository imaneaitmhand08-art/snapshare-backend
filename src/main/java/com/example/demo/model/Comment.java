package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * AVEC SPRING JPA : @Entity crée automatiquement la table comments
 * SANS SPRING : CREATE TABLE comments (id BIGINT AUTO_INCREMENT,
 * text TEXT, created_at DATETIME, user_id BIGINT, post_id BIGINT)
 * + gestion manuelle des clés étrangères via JDBC
 */
@Data
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * AVEC SPRING : @ManyToOne EAGER charge l'user automatiquement
     * SANS SPRING : stocker user_id et faire SELECT * FROM users WHERE id = ?
     * puis construire l'objet User manuellement depuis le ResultSet
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"posts", "likedPosts", "following", "followers", "password"})
    private User user;

    /**
     * AVEC SPRING : @ManyToOne gère la relation automatiquement
     * SANS SPRING : stocker post_id et faire SELECT * FROM posts WHERE id = ?
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @JsonIgnoreProperties({"comments", "likedBy", "user"})
    private Post post;
}