package com.example.demo.repository;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * AVEC SPRING DATA JPA : méthodes CRUD générées automatiquement
 * SANS SPRING : "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at DESC"
 * via JDBC avec Connection, PreparedStatement, ResultSet
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * AVEC SPRING : Spring génère la requête automatiquement depuis le nom de la méthode
     * SANS SPRING : requête SQL manuelle avec jointure sur la table posts
     */
    List<Comment> findByPostOrderByCreatedAtDesc(Post post);
}