package com.qrqueue.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void broadcastQueueUpdate(Long counterId) {
        messagingTemplate.convertAndSend("/topic/queue/" + counterId, "Queue updated");
    }

    public void notifyUserTurn(Long queueId) {
        messagingTemplate.convertAndSend("/topic/user/" + queueId, "It's almost your turn!");
    }
}
