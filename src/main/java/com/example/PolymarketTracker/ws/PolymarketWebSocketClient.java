package com.example.PolymarketTracker.ws;

import com.example.PolymarketTracker.config.PolymarketProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PolymarketWebSocketClient extends TextWebSocketHandler {

    private static final String WS_URL = "wss://ws-subscriptions-clob.polymarket.com/ws/market";

    private final WebSocketClient webSocketClient;
    private final PolymarketMessageHandler messageHandler;
    private final PolymarketProperties polymarketProperties;
    private final ObjectMapper objectMapper;

    private WebSocketSession session;
    private WebSocketConnectionManager connectionManager;
    private ScheduledExecutorService heartbeatExecutor;

    public PolymarketWebSocketClient(WebSocketClient webSocketClient,
                                     PolymarketMessageHandler messageHandler,
                                     PolymarketProperties polymarketProperties) {
        this.webSocketClient = webSocketClient;
        this.messageHandler = messageHandler;
        this.polymarketProperties = polymarketProperties;
        this.objectMapper = new ObjectMapper();
    }

    @PostConstruct
    public void connect() {
        log.info("Connecting to Polymarket WebSocket: {}", WS_URL);
        connectionManager = new WebSocketConnectionManager(webSocketClient, this, WS_URL);
        connectionManager.start();

        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("✅ WebSocket connection established");
        this.session = session;
        sendSubscription();
    }

    private void sendSubscription() {
        try {
            List<String> assetIds = polymarketProperties.getAssetIds();

            if (assetIds == null || assetIds.isEmpty()) {
                log.warn("No asset IDs configured. Please add polymarket.asset-ids to application.yml");
                return;
            }

            Map<String, Object> subscription = new HashMap<>();
            subscription.put("type", "market");
            subscription.put("assets_ids", assetIds);
            subscription.put("custom_feature_enabled", true);

            String subscriptionJson = objectMapper.writeValueAsString(subscription);
            session.sendMessage(new TextMessage(subscriptionJson));
            log.info("📡 Subscribed to {} assets: {}", assetIds.size(), assetIds);
        } catch (Exception e) {
            log.error("Failed to send subscription", e);
        }
    }

    private void sendHeartbeat() {
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage("PING"));
                log.debug("💓 PING sent");
            } catch (Exception e) {
                log.warn("Failed to send heartbeat", e);
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        messageHandler.handle(message.getPayload());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket error", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("Connection closed: {}", status);
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            log.info("Reconnecting...");
            connect();
        }, 5, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void cleanup() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        if (connectionManager != null) {
            connectionManager.stop();
        }
    }
}