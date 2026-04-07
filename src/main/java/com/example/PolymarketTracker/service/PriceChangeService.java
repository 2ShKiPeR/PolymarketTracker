package com.example.PolymarketTracker.service;

import com.example.PolymarketTracker.model.PriceChange;
import com.example.PolymarketTracker.repository.PriceChangeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceChangeService {

    private final PriceChangeRepository repository;

    public void save(String assetId, BigDecimal oldPrice, BigDecimal newPrice, long timestamp) {
        PriceChange change = PriceChange.builder()
                .assetId(assetId)
                .oldPrice(oldPrice)
                .newPrice(newPrice)
                .changeTimestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        repository.save(change);
        log.info("Saved price change for {}: {} -> {}", assetId, oldPrice, newPrice);
    }
}