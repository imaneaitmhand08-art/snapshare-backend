package com.example.demo.controller;

import com.example.demo.model.Comment;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.PostService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    private Map<String, Object> postToMap(Post post) {
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
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            post.setUser(user);
            Post saved = postService.createPost(post);
            return ResponseEntity.ok(postToMap(saved));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllPosts() {
        try {
            List<Post> posts = postService.getAllPosts();
            List<Map<String, Object>> result = new ArrayList<>();
            for (Post post : posts) {
                result.add(postToMap(post));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/foryou")
    public ResponseEntity<?> getForYouPosts(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            List<Post> posts = postService.getForYouPosts(user);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Post post : posts) {
                result.add(postToMap(post));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchPosts(@RequestParam String q) {
        try {
            List<Post> posts = postService.searchPosts(q);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Post post : posts) {
                result.add(postToMap(post));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPostsByUser(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            List<Post> posts = postService.getPostsByUser(user);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Post post : posts) {
                result.add(postToMap(post));
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            Post post = postService.likePost(id, user);

            // ✅ Notification de like
            if (post.getUser() != null) {
                notificationService.createNotification(
                    "LIKE",
                    user.getUsername() + " a aimé votre post",
                    post.getUser(),
                    user,
                    post
                );
            }

            return ResponseEntity.ok(postToMap(post));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok("Post supprimé");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            Comment comment = postService.addComment(id, user, body.get("text"));

            // ✅ Notification de commentaire
            if (comment.getPost().getUser() != null) {
                notificationService.createNotification(
                    "COMMENT",
                    user.getUsername() + " a commenté votre post",
                    comment.getPost().getUser(),
                    user,
                    comment.getPost()
                );
            }

            Map<String, Object> result = new HashMap<>();
            result.put("id", comment.getId());
            result.put("text", comment.getText());
            result.put("createdAt", comment.getCreatedAt());
            if (comment.getUser() != null) {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", comment.getUser().getId());
                userMap.put("username", comment.getUser().getUsername());
                result.put("user", userMap);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long id) {
        try {
            List<Comment> comments = postService.getCommentsByPost(id);
            List<Map<String, Object>> result = new ArrayList<>();
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", comment.getId());
                map.put("text", comment.getText());
                map.put("createdAt", comment.getCreatedAt());
                if (comment.getUser() != null) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", comment.getUser().getId());
                    userMap.put("username", comment.getUser().getUsername());
                    map.put("user", userMap);
                }
                result.add(map);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            postService.deleteComment(commentId);
            return ResponseEntity.ok("Commentaire supprimé");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}