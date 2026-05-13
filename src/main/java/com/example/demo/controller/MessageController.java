package com.example.demo.controller;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.service.MessageService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
 private com.example.demo.repository.MessageRepository messageRepository;

    // Envoyer un message
    @PostMapping("/{receiverId}")
    public ResponseEntity<?> sendMessage(
            @PathVariable Long receiverId,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        try {
            User sender = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            User receiver = userService.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Destinataire non trouvé"));

            Message message = messageService.sendMessage(sender, receiver, body.get("content"));

            Map<String, Object> result = messageToMap(message);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Récupérer la conversation avec un user
    @GetMapping("/conversation/{userId}")
    public ResponseEntity<?> getConversation(
            @PathVariable Long userId,
            Authentication auth) {
        try {
            User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            User otherUser = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<Message> messages = messageService.getConversation(currentUser, otherUser);
            messageService.markAsRead(otherUser, currentUser);

            List<Map<String, Object>> result = messages.stream()
                .map(this::messageToMap)
                .toList();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Récupérer la liste des conversations
    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations(Authentication auth) {
        try {
            User currentUser = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            List<Message> allMessages = messageService.getAllMessages(currentUser);

            // Garder seulement le dernier message par conversation
            Map<Long, Map<String, Object>> conversations = new LinkedHashMap<>();
            for (Message m : allMessages) {
                User other = m.getSender().getId().equals(currentUser.getId())
                    ? m.getReceiver() : m.getSender();

                if (!conversations.containsKey(other.getId())) {
                    Map<String, Object> conv = new HashMap<>();
                    conv.put("userId", other.getId());
                    conv.put("username", other.getUsername());
                    conv.put("lastMessage", m.getContent());
                    conv.put("lastMessageDate", m.getCreatedAt());
                    conv.put("isRead", m.isRead() || m.getSender().getId().equals(currentUser.getId()));
                    conversations.put(other.getId(), conv);
                }
            }

            return ResponseEntity.ok(new ArrayList<>(conversations.values()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    // Compter les messages non lus
    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount(Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            long count = messageService.countUnread(user);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    private Map<String, Object> messageToMap(Message message) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", message.getId());
        map.put("content", message.getContent());
        map.put("createdAt", message.getCreatedAt());
        map.put("read", message.isRead());
        if (message.getSender() != null) {
            Map<String, Object> sender = new HashMap<>();
            sender.put("id", message.getSender().getId());
            sender.put("username", message.getSender().getUsername());
            map.put("sender", sender);
        }
        if (message.getReceiver() != null) {
            Map<String, Object> receiver = new HashMap<>();
            receiver.put("id", message.getReceiver().getId());
            receiver.put("username", message.getReceiver().getUsername());
            map.put("receiver", receiver);
        }
        return map;
    }
    @DeleteMapping("/{id}")
public ResponseEntity<?> deleteMessage(@PathVariable Long id, Authentication auth) {
    try {
        messageRepository.deleteById(id);
        return ResponseEntity.ok("Message supprimé");
    } catch (Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
    }
}
}