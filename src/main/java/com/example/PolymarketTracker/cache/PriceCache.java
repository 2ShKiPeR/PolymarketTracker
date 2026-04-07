package com.example.PolymarketTracker.cache;

import java.math.BigDecimal;

public interface PriceCache {
    BigDecimal get(String key);
    void put(String key, BigDecimal value);
}