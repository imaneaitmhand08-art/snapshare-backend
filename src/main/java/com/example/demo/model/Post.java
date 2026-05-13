package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * AVEC SPRING JPA : @Entity crée automatiquement la table posts
 * SANS SPRING : CREATE TABLE posts (id BIGINT AUTO_INCREMENT, content TEXT,
 * image_url VARCHAR(255), created_at DATETIME, user_id BIGINT)
 */
@Getter
@Setter
@Entity
@Table(name = "posts")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    /**
     * AVEC SPRING : @Column(columnDefinition = "LONGTEXT") permet de stocker
     * des images en base64 (très long texte)
     * SANS SPRING : ALTER TABLE posts MODIFY image_url LONGTEXT via SQL manuel
     */
    @Column(columnDefinition = "LONGTEXT")
    private String imageUrl;

    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * AVEC SPRING : @ManyToOne + @JoinColumn gère la clé étrangère automatiquement
     * SANS SPRING : stocker user_id et faire un JOIN SQL manuellement
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"posts", "likedPosts", "following", "followers", "password"})
    private User user;

    /**
     * AVEC SPRING : @OneToMany gère la relation avec les commentaires
     * SANS SPRING : SELECT * FROM comments WHERE post_id = ? via JDBC
     */
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"post"})
    private Set<Comment> comments = new HashSet<>();

    /**
     * AVEC SPRING : @ManyToMany mappedBy = relation gérée par User.likedPosts
     * SANS SPRING : SELECT user_id FROM likes WHERE post_id = ? via JDBC
     */
    @ManyToMany(mappedBy = "likedPosts", fetch = FetchType.EAGER)
    @JsonIgnoreProperties({"posts", "likedPosts", "following", "followers", "password"})
    private Set<User> likedBy = new HashSet<>();
}