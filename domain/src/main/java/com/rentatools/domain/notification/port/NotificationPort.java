package com.rentatools.domain.notification.port;

public interface NotificationPort {
    void sendToClient(Object clientId, String destination, Object payload);
    void sendToProvider(Object providerId, String destination, Object payload);
    void broadcast(String destination, Object payload);
}
