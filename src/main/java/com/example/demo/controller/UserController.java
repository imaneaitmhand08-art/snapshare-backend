package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.FollowRepository;
import com.example.demo.repository.PostRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private NotificationService notificationService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            User saved = userService.registerUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("username", saved.getUsername());
            response.put("email", saved.getEmail());
            response.put("role", saved.getRole());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(Authentication auth) {
        try {
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(401).body("Non autorisé");
            }
            return userService.findByEmail(auth.getName())
                .map(user -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("id", user.getId());
                    response.put("username", user.getUsername());
                    response.put("email", user.getEmail());
                    response.put("role", user.getRole());
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.status(401).build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/users/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        return userService.findByEmail(auth.getName())
            .map(user -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("email", user.getEmail());
                response.put("bio", user.getBio());
                response.put("profilePicture", user.getProfilePicture());
                response.put("role", user.getRole());
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/me")
    public ResponseEntity<?> updateProfile(
            @RequestBody Map<String, Object> body,
            Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (body.containsKey("bio")) {
                user.setBio((String) body.get("bio"));
            }
            if (body.containsKey("profilePicture")) {
                user.setProfilePicture((String) body.get("profilePicture"));
            }

            User saved = userService.saveUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", saved.getId());
            response.put("username", saved.getUsername());
            response.put("email", saved.getEmail());
            response.put("bio", saved.getBio());
            response.put("profilePicture", saved.getProfilePicture());
            response.put("role", saved.getRole());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/users/me/following")
    public ResponseEntity<?> getMyFollowing(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<com.example.demo.model.Follow> follows = followRepository.findByFollower(user);

            List<Map<String, Object>> result = follows.stream().map(f -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", f.getFollowing().getId());
                map.put("username", f.getFollowing().getUsername());
                return map;
            }).toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/users/{id}/isFollowing")
    public ResponseEntity<?> isFollowing(@PathVariable Long id, Authentication auth) {
        User follower = userService.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User following = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        boolean isFollowing = followRepository.existsByFollowerAndFollowing(follower, following);
        return ResponseEntity.ok(Map.of("isFollowing", isFollowing));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(user -> {
                Map<String, Object> response = new HashMap<>();
                response.put("id", user.getId());
                response.put("username", user.getUsername());
                response.put("role", user.getRole());
                response.put("bio", user.getBio());
                long followersCount = followRepository.findByFollowing(user).size();
                long followingCount = followRepository.findByFollower(user).size();
                response.put("followersCount", followersCount);
                response.put("followingCount", followingCount);
                return ResponseEntity.ok(response);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/search")
    public ResponseEntity<?> searchUsers(@RequestParam String q) {
        List<User> users = userService.searchUsers(q);
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/{id}/follow")
    public ResponseEntity<?> followUser(@PathVariable Long id, Authentication auth) {
        User follower = userService.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User following = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        userService.followUser(follower, following);

        // ✅ Notification de follow
        notificationService.createNotification(
            "FOLLOW",
            follower.getUsername() + " a commencé à vous suivre",
            following,
            follower,
            null
        );

        return ResponseEntity.ok("Abonné avec succès");
    }

    @DeleteMapping("/users/{id}/unfollow")
    public ResponseEntity<?> unfollowUser(@PathVariable Long id, Authentication auth) {
        User follower = userService.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        User following = userService.findById(id)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        userService.unfollowUser(follower, following);
        return ResponseEntity.ok("Désabonné avec succès");
    }

    @GetMapping("/admin/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("Utilisateur supprimé");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsersPublic() {
        List<User> users = userService.getAllUsers();
        List<Map<String, Object>> result = users.stream().map(user -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("role", user.getRole());
            return map;
        }).toList();
        return ResponseEntity.ok(result);
    }

    // ✅ Bookmarks
    @PostMapping("/users/bookmarks/{postId}")
    public ResponseEntity<?> toggleBookmark(@PathVariable Long postId, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            com.example.demo.model.Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post non trouvé"));

            boolean isBookmarked = user.getBookmarks().stream()
                .anyMatch(p -> p.getId().equals(postId));

            if (isBookmarked) {
                user.getBookmarks().removeIf(p -> p.getId().equals(postId));
            } else {
                user.getBookmarks().add(post);
            }
            userService.saveUser(user);
            return ResponseEntity.ok(Map.of("bookmarked", !isBookmarked));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/users/bookmarks")
    public ResponseEntity<?> getBookmarks(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<Map<String, Object>> result = user.getBookmarks().stream().map(post -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", post.getId());
                map.put("content", post.getContent());
                map.put("imageUrl", post.getImageUrl());
                map.put("createdAt", post.getCreatedAt());
                map.put("likesCount", post.getLikedBy() != null ? post.getLikedBy().size() : 0);
                if (post.getUser() != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", post.getUser().getId());
                    userMap.put("username", post.getUser().getUsername());
                    map.put("user", userMap);
                }
                return map;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/users/bookmarks/{postId}/check")
    public ResponseEntity<?> checkBookmark(@PathVariable Long postId, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            boolean isBookmarked = user.getBookmarks().stream()
                .anyMatch(p -> p.getId().equals(postId));
            return ResponseEntity.ok(Map.of("bookmarked", isBookmarked));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
    @GetMapping("/users/suggestions")
public ResponseEntity<?> getSuggestions(Authentication auth) {
    try {
        User currentUser = userService.findByEmail(auth.getName())
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Récupérer les IDs des gens déjà suivis
        List<Long> followingIds = followRepository.findByFollower(currentUser)
            .stream()
            .map(f -> f.getFollowing().getId())
            .toList();

        // Tous les users sauf soi-même et ceux déjà suivis
        List<User> suggestions = userService.getAllUsers()
            .stream()
            .filter(u -> !u.getId().equals(currentUser.getId()))
            .filter(u -> !followingIds.contains(u.getId()))
            // Trier par nombre d'abonnés
            .sorted((a, b) -> 
                followRepository.findByFollowing(b).size() - 
                followRepository.findByFollowing(a).size()
            )
            .limit(5)
            .toList();

        List<Map<String, Object>> result = suggestions.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("bio", u.getBio());
            map.put("followersCount", followRepository.findByFollowing(u).size());
            return map;
        }).toList();

        return ResponseEntity.ok(result);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(e.getMessage());
    }
}
}