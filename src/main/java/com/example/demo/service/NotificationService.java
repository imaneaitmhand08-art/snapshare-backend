package com.example.demo.service;

import com.example.demo.model.Notification;
import com.example.demo.model.Post;
import com.example.demo.model.User;
import com.example.demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    // Créer une notification
    public void createNotification(String type, String message, User receiver, User sender, Post post) {
        // Ne pas notifier si c'est soi-même
        if (receiver.getId().equals(sender.getId())) return;

        Notification notification = new Notification();
        notification.setType(type);
        notification.setMessage(message);
        notification.setReceiver(receiver);
        notification.setSender(sender);
        notification.setPost(post);
        notificationRepository.save(notification);
    }

    // Récupérer les notifications d'un user
    public List<Notification> getNotifications(User user) {
        return notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
    }

    // Compter les non lues
    public long countUnread(User user) {
        return notificationRepository.countByReceiverAndReadIsFalse(user);
    }

    // Marquer toutes comme lues
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByReceiverOrderByCreatedAtDesc(user);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }

    // Supprimer une notification
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}