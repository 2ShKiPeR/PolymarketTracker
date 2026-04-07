package com.example.PolymarketTracker.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PolymarketHttpClient {

    private static final String CLOB_API_URL = "https://clob.polymarket.com";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String getMarkets() {
        try {
            String url = CLOB_API_URL + "/markets";

            // Исправленный запрос - убираем queryString с null
            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .queryString("limit", "100")  // Используем String
                    // Убираем .queryString("next_cursor", null)
                    .asString();

            if (response.getStatus() != 200) {
                log.error("Failed to fetch markets, status: {}", response.getStatus());
                throw new RuntimeException("HTTP " + response.getStatus());
            }

            log.debug("Successfully fetched markets, response length: {}",
                    response.getBody() != null ? response.getBody().length() : 0);
            return response.getBody();

        } catch (Exception e) {
            log.error("Error fetching markets from CLOB API", e);
            return getMarketsFromGamma(); // Fallback
        }
    }

    private String getMarketsFromGamma() {
        try {
            String url = "https://gamma-api.polymarket.com/markets";

            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .queryString("active", "true")
                    .queryString("limit", "50")
                    .asString();

            if (response.getStatus() == 200) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.error("Gamma API also failed", e);
        }

        // Если оба API не работают, возвращаем пустой массив
        return "[]";
    }

    public Double getMarketPrice(String marketId) {
        try {
            String url = CLOB_API_URL + "/markets/" + marketId;

            HttpResponse<String> response = Unirest.get(url)
                    .header("Accept", "application/json")
                    .asString();

            if (response.getStatus() == 200 && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());

                // Пробуем разные поля с ценой
                if (json.has("price")) {
                    return json.get("price").asDouble();
                } else if (json.has("mid_price")) {
                    return json.get("mid_price").asDouble();
                } else if (json.has("market") && json.get("market").has("price")) {
                    return json.get("market").get("price").asDouble();
                }
            }
        } catch (Exception e) {
            log.error("Failed to get price for market: {}", marketId, e);
        }
        return null;
    }
}