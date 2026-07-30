package com.helpdesk.helpdesk_backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyTicketUpdate(String type, 
        Object data) {
        // messagingTemplate.convertAndSend(
        //     "/topic/tickets",
        //     Map.of("type", type, "data", data)
        // );

messagingTemplate.convertAndSend(
    "/topic/tickets",
    (Object) Map.of("type", type, "data", data)
);
    }

    public void notifyNewComment(Object data) {
        messagingTemplate.convertAndSend(
            "/topic/comments", data);
    }
}