package com.rentatools.domain.notification.port;

public interface NotificationPort {
    void sendToClient(Long clientId, String destination, Object payload);
    void sendToProvider(Long providerId, String destination, Object payload);
    void broadcast(String destination, Object payload);
}
