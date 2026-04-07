package com.example.PolymarketTracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "price_changes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceChange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "asset_id", nullable = false)
    private String assetId;

    @Column(name = "old_price", precision = 19, scale = 6)
    private BigDecimal oldPrice;

    @Column(name = "new_price", precision = 19, scale = 6)
    private BigDecimal newPrice;

    @Column(name = "change_timestamp", nullable = false)
    private LocalDateTime changeTimestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}