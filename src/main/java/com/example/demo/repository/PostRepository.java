package com.example.demo.repository;

import com.example.demo.model.Post;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * AVEC SPRING DATA JPA : hérite de toutes les méthodes CRUD automatiquement
 * SANS SPRING : écrire chaque requête SQL manuellement dans un DAO
 * ex: "SELECT * FROM posts ORDER BY created_at DESC"
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * AVEC SPRING : Spring génère "SELECT * FROM posts WHERE user_id = ? ORDER BY created_at DESC"
     * SANS SPRING : requête SQL + parcours du ResultSet manuellement
     */
    List<Post> findByUserOrderByCreatedAtDesc(User user);

    List<Post> findAllByOrderByCreatedAtDesc();

    /**
     * AVEC SPRING : @Query avec JPQL pour recherche par mot-clé
     * SANS SPRING : "SELECT * FROM posts WHERE content LIKE '%?%'"
     */
    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% ORDER BY p.createdAt DESC")
    List<Post> searchByContent(String keyword);


    // Posts des utilisateurs suivis
@Query("SELECT p FROM Post p WHERE p.user IN :users ORDER BY p.createdAt DESC")
List<Post> findByUserInOrderByCreatedAtDesc(List<User> users);

// Posts les plus likés
@Query("SELECT p FROM Post p ORDER BY SIZE(p.likedBy) DESC")
List<Post> findAllOrderByLikesDesc();



}