package com.example.PolymarketTracker.service;

import com.example.PolymarketTracker.model.PriceChange;
import com.example.PolymarketTracker.repository.PriceChangeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceChangeServiceTest {

    @Mock
    private PriceChangeRepository repository;

    @InjectMocks
    private PriceChangeService priceChangeService;

    @Test
    void shouldSavePriceChange() {
        // given
        String assetId = "test-asset";
        BigDecimal oldPrice = new BigDecimal("0.50");
        BigDecimal newPrice = new BigDecimal("0.55");
        long timestamp = System.currentTimeMillis();

        // when
        priceChangeService.save(assetId, oldPrice, newPrice, timestamp);

        // then
        ArgumentCaptor<PriceChange> captor = ArgumentCaptor.forClass(PriceChange.class);
        verify(repository).save(captor.capture());

        PriceChange saved = captor.getValue();
        assertThat(saved.getAssetId()).isEqualTo(assetId);
        assertThat(saved.getOldPrice()).isEqualTo(oldPrice);
        assertThat(saved.getNewPrice()).isEqualTo(newPrice);
        assertThat(saved.getChangeTimestamp()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldSaveWithNullOldPrice() {
        // given
        String assetId = "test-asset";
        BigDecimal newPrice = new BigDecimal("0.55");
        long timestamp = System.currentTimeMillis();

        // when
        priceChangeService.save(assetId, null, newPrice, timestamp);

        // then
        ArgumentCaptor<PriceChange> captor = ArgumentCaptor.forClass(PriceChange.class);
        verify(repository).save(captor.capture());

        PriceChange saved = captor.getValue();
        assertThat(saved.getAssetId()).isEqualTo(assetId);
        assertThat(saved.getOldPrice()).isNull();
        assertThat(saved.getNewPrice()).isEqualTo(newPrice);
    }
}