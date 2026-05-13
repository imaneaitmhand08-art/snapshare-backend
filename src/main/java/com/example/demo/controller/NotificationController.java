package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.model.User;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // Récupérer toutes les notifications
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
            List<Notification> notifications = notificationService.getNotifications(user);
            List<Map<String, Object>> result = notifications.stream().map(n -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", n.getId());
                map.put("type", n.getType());
                map.put("message", n.getMessage());
                map.put("read", n.isRead());
                map.put("createdAt", n.getCreatedAt());
                if (n.getSender() != null) {
                    Map<String, Object> sender = new HashMap<>();
                    sender.put("id", n.getSender().getId());
                    sender.put("username", n.getSender().getUsername());
                    map.put("sender", sender);
                }
                if (n.getPost() != null) {
                    map.put("postId", n.getPost().getId());
                }
                return map;
            }).toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Compter les non lues
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            long count = notificationService.countUnread(user);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Marquer toutes comme lues
    @PutMapping("/mark-all-read")
    public ResponseEntity<?> markAllAsRead(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            notificationService.markAllAsRead(user);
            return ResponseEntity.ok("Notifications marquées comme lues");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Supprimer une notification
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable Long id) {
        try {
            notificationService.deleteNotification(id);
            return ResponseEntity.ok("Notification supprimée");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}