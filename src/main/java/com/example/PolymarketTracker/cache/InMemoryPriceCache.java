package com.example.PolymarketTracker.cache;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPriceCache implements PriceCache {
    private final ConcurrentHashMap<String, BigDecimal> cache = new ConcurrentHashMap<>();

    @Override
    public BigDecimal get(String key) {
        return cache.get(key);
    }

    @Override
    public void put(String key, BigDecimal value) {
        cache.put(key, value);
    }


}