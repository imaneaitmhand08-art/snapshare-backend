package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * AVEC SPRING DATA JPA : JpaRepository fournit automatiquement
 * save(), findById(), findAll(), delete() sans écrire une seule ligne SQL
 *
 * SANS SPRING : il faudrait écrire un DAO complet :
 * Connection con = DriverManager.getConnection(url, user, password);
 * PreparedStatement ps = con.prepareStatement("SELECT * FROM users WHERE email = ?");
 * ps.setString(1, email);
 * ResultSet rs = ps.executeQuery();
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * AVEC SPRING : Spring génère automatiquement la requête SQL à partir du nom
     * SANS SPRING : "SELECT * FROM users WHERE email = ?"
     */
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    /**
     * AVEC SPRING : @Query permet d'écrire du JPQL (Java Persistence Query Language)
     * SANS SPRING : "SELECT * FROM users WHERE username LIKE '%?%'"
     */
    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword%")
    List<User> searchByUsername(String keyword);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}