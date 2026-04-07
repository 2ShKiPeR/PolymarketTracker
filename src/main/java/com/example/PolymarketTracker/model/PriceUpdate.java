package com.example.PolymarketTracker.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PriceUpdate {
    private String assetId;
    private BigDecimal price;
    private long timestamp;
}