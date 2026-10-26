package com.rentatools.infrastructure.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Soporte tanto para WebSocket puro como para SockJS fallback
        registry.addEndpoint("/ws-rentatools")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        registry.addEndpoint("/ws-rentatools")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Habilita broker en memoria para tópicos públicos y colas privadas
        registry.enableSimpleBroker("/topic", "/queue");
        // Prefijo para mensajes entrantes desde el cliente al servidor
        registry.setApplicationDestinationPrefixes("/app");
    }
}
