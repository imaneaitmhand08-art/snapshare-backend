package com.example.demo.repository;

import com.example.demo.model.Follow;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * AVEC SPRING DATA JPA : méthodes générées automatiquement
 * SANS SPRING : "SELECT * FROM follows WHERE follower_id = ?"
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    List<Follow> findByFollower(User follower);
    List<Follow> findByFollowing(User following);
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    boolean existsByFollowerAndFollowing(User follower, User following);
}