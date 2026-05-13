package com.example.demo.service;

import com.example.demo.model.Message;
import com.example.demo.model.User;
import com.example.demo.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    // Envoyer un message
    public Message sendMessage(User sender, User receiver, String content) {
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(content);
        return messageRepository.save(message);
    }

    // Récupérer la conversation entre 2 users
    public List<Message> getConversation(User user1, User user2) {
        return messageRepository.findConversation(user1, user2);
    }

    // Récupérer toutes les conversations d'un user
    public List<Message> getAllMessages(User user) {
        return messageRepository.findAllByUser(user);
    }

    // Compter les messages non lus
    public long countUnread(User user) {
        return messageRepository.countByReceiverAndReadFalse(user);
    }

    // Marquer les messages comme lus
    public void markAsRead(User sender, User receiver) {
        List<Message> messages = messageRepository.findConversation(sender, receiver);
        messages.forEach(m -> {
            if (m.getReceiver().getId().equals(receiver.getId())) {
                m.setRead(true);
            }
        });
        messageRepository.saveAll(messages);
    }
}