package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

/**
 * AVEC SPRING JPA : @Entity crée automatiquement la table follows
 * SANS SPRING : CREATE TABLE follows (id BIGINT, follower_id BIGINT, following_id BIGINT)
 * + INSERT INTO follows VALUES (?, ?) via JDBC manuellement
 */
@Data
@Entity
@Table(name = "follows")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * AVEC SPRING : @ManyToOne + @JoinColumn gère la clé étrangère
     * SANS SPRING : stocker follower_id et faire une requête SQL JOIN manuellement
     */
    @ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "follower_id")
private User follower;

@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "following_id")
private User following;
}