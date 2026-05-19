package com.doomhamsters.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/** Configures STOMP over WebSocket for real-time lobby and game communication. */
@Configuration
@EnableWebSocketMessageBroker
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Creates the WebSocket message broker configuration.
   */
  public WebSocketConfig() {
    // Required by Spring so it can instantiate this configuration class.
  }

  /**
   * Sets up the in-memory message broker.
   *
   * <p>/topic — broadcast to all subscribers (e.g. game-state updates)<br>
   * /queue — messages directed to a single client (e.g. private hand)<br>
   * /app — prefix clients use to send messages to @MessageMapping handlers
   *
   * @param config broker registry to configure
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", "/queue");
    config.setApplicationDestinationPrefixes("/app");
  }

  /**
   * Registers the WebSocket handshake endpoint.
   *
   * <p>Clients connect to ws://localhost:53217/ws
   * Android client uses Krossbow with native soch
   *
   * @param registry STOMP endpoint registry
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }
}
