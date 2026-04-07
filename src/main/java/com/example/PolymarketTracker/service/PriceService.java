package com.example.PolymarketTracker.service;

import com.example.PolymarketTracker.cache.PriceCache;
import com.example.PolymarketTracker.model.PriceUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceService {

    private final PriceCache cache;
    private final PriceChangeService changeService;

    public void process(PriceUpdate update) {
        String assetId = update.getAssetId();
        BigDecimal newPrice = update.getPrice();

        if (assetId == null || newPrice == null) {
            log.warn("Invalid price update");
            return;
        }

        BigDecimal oldPrice = cache.get(assetId);

        if (oldPrice == null || oldPrice.compareTo(newPrice) != 0) {
            log.info("Price changed for {}: {} -> {}", assetId, oldPrice, newPrice);
            changeService.save(assetId, oldPrice, newPrice, update.getTimestamp());
            cache.put(assetId, newPrice);
        }
    }
}