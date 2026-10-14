package com.rentatools.infrastructure.websocket;

import com.rentatools.domain.notification.port.NotificationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StompNotificationService implements NotificationPort {
    private static final Logger log = LoggerFactory.getLogger(StompNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public StompNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void sendToClient(Object clientId, String destination, Object payload) {
        String target = destination != null ? destination : "/topic/client/" + clientId;
        log.info("Emitiendo notificación WebSocket a Cliente [{}]: {}", target, payload);
        Object wrapped = createPayloadWrapper(payload);
        messagingTemplate.convertAndSend(target, wrapped);
    }

    @Override
    public void sendToProvider(Object providerId, String destination, Object payload) {
        String target = destination != null ? destination : "/topic/provider/" + providerId;
        log.info("Emitiendo notificación WebSocket a Proveedor [{}]: {}", target, payload);
        Object wrapped = createPayloadWrapper(payload);
        messagingTemplate.convertAndSend(target, wrapped);
    }

    @Override
    public void broadcast(String destination, Object payload) {
        String target = destination != null ? destination : "/topic/catalog";
        log.info("Emitiendo broadcast WebSocket [{}]: {}", target, payload);
        Object wrapped = createPayloadWrapper(payload);
        messagingTemplate.convertAndSend(target, wrapped);
    }

    private Map<String, Object> createPayloadWrapper(Object payload) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", LocalDateTime.now().toString());
        map.put("data", payload);
        return map;
    }
}
