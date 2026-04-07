package com.example.PolymarketTracker.ws;

import com.example.PolymarketTracker.model.PriceUpdate;
import com.example.PolymarketTracker.service.PriceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
@Slf4j
@RequiredArgsConstructor
public class PolymarketMessageHandler {

    private final ObjectMapper objectMapper;
    private final PriceService priceService;

    public void handle(String message) {
        // Игнорируем PONG и другие не-JSON сообщения
        if (message == null || message.trim().isEmpty() || "PONG".equals(message.trim())) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.has("event_type") ? root.get("event_type").asText() : null;

            // Нас интересуют только price_change
            if ("price_change".equals(eventType)) {
                handlePriceChange(root);
            }
        } catch (Exception e) {
            log.debug("Failed to parse message: {}", message);
        }
    }

    private void handlePriceChange(JsonNode root) {
        try {
            JsonNode changes = root.get("price_changes");
            if (changes != null && changes.isArray()) {
                for (JsonNode change : changes) {
                    String assetId = change.get("asset_id").asText();
                    double price = change.get("price").asDouble();
                    long timestamp = root.has("timestamp") ? root.get("timestamp").asLong() : System.currentTimeMillis();

                    log.info("💰 Price change - Asset: {}, Price: {}", assetId, price);

                    PriceUpdate update = PriceUpdate.builder()
                            .assetId(assetId)
                            .price(BigDecimal.valueOf(price))
                            .timestamp(timestamp)
                            .build();

                    priceService.process(update);
                }
            }
        } catch (Exception e) {
            log.error("Error processing price change", e);
        }
    }
}