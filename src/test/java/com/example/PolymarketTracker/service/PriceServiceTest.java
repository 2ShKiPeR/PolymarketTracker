package com.example.PolymarketTracker.service;

import com.example.PolymarketTracker.cache.PriceCache;
import com.example.PolymarketTracker.model.PriceUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceServiceTest {

    @Mock
    private PriceCache cache;

    @Mock
    private PriceChangeService changeService;

    @InjectMocks
    private PriceService priceService;

    private final String testAssetId = "test-asset-123";
    private final BigDecimal oldPrice = new BigDecimal("0.50");
    private final BigDecimal newPrice = new BigDecimal("0.55");

    @Test
    void shouldSaveWhenPriceChanged() {
        // given
        when(cache.get(testAssetId)).thenReturn(oldPrice);

        PriceUpdate update = PriceUpdate.builder()
                .assetId(testAssetId)
                .price(newPrice)
                .timestamp(System.currentTimeMillis())
                .build();

        // when
        priceService.process(update);

        // then
        verify(changeService).save(eq(testAssetId), eq(oldPrice), eq(newPrice), anyLong());
        verify(cache).put(testAssetId, newPrice);
    }

    @Test
    void shouldNotSaveWhenPriceSame() {
        // given
        when(cache.get(testAssetId)).thenReturn(newPrice);

        PriceUpdate update = PriceUpdate.builder()
                .assetId(testAssetId)
                .price(newPrice)
                .timestamp(System.currentTimeMillis())
                .build();

        // when
        priceService.process(update);

        // then
        verify(changeService, never()).save(any(), any(), any(), anyLong());
        verify(cache, never()).put(any(), any());
    }

    @Test
    void shouldSaveWhenNoOldPrice() {
        // given
        when(cache.get(testAssetId)).thenReturn(null);

        PriceUpdate update = PriceUpdate.builder()
                .assetId(testAssetId)
                .price(newPrice)
                .timestamp(System.currentTimeMillis())
                .build();

        // when
        priceService.process(update);

        // then
        verify(changeService).save(eq(testAssetId), isNull(), eq(newPrice), anyLong());
        verify(cache).put(testAssetId, newPrice);
    }

    @Test
    void shouldIgnoreInvalidUpdate() {
        // given
        PriceUpdate update = PriceUpdate.builder()
                .assetId(null)
                .price(newPrice)
                .timestamp(System.currentTimeMillis())
                .build();

        // when
        priceService.process(update);

        // then
        verify(changeService, never()).save(any(), any(), any(), anyLong());
        verify(cache, never()).put(any(), any());
    }
}