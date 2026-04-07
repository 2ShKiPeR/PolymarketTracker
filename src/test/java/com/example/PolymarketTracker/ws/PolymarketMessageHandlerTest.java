package com.example.PolymarketTracker.ws;

import com.example.PolymarketTracker.model.PriceUpdate;
import com.example.PolymarketTracker.service.PriceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolymarketMessageHandlerTest {

    @Mock
    private PriceService priceService;

    private ObjectMapper objectMapper;
    private PolymarketMessageHandler messageHandler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        messageHandler = new PolymarketMessageHandler(objectMapper, priceService);
    }

    @Test
    void shouldParsePriceChangeMessage() {
        // given
        String message = """
            {
                "event_type": "price_change",
                "price_changes": [
                    {
                        "asset_id": "asset-123",
                        "price": 0.65,
                        "size": 100,
                        "side": "BUY"
                    }
                ],
                "timestamp": 1734567890000
            }
            """;

        // when
        messageHandler.handle(message);

        // then
        ArgumentCaptor<PriceUpdate> captor = ArgumentCaptor.forClass(PriceUpdate.class);
        verify(priceService).process(captor.capture());

        PriceUpdate update = captor.getValue();
        assertThat(update.getAssetId()).isEqualTo("asset-123");
        assertThat(update.getPrice()).isEqualTo(new BigDecimal("0.65"));
        assertThat(update.getTimestamp()).isEqualTo(1734567890000L);
    }

    @Test
    void shouldParseMultiplePriceChanges() {
        // given
        String message = """
            {
                "event_type": "price_change",
                "price_changes": [
                    {
                        "asset_id": "asset-1",
                        "price": 0.65
                    },
                    {
                        "asset_id": "asset-2",
                        "price": 0.35
                    }
                ],
                "timestamp": 1734567890000
            }
            """;

        // when
        messageHandler.handle(message);

        // then
        verify(priceService, times(2)).process(any(PriceUpdate.class));
    }

    @Test
    void shouldIgnorePongMessage() {
        // given
        String message = "PONG";

        // when
        messageHandler.handle(message);

        // then
        verify(priceService, never()).process(any());
    }

    @Test
    void shouldIgnoreNonJsonMessage() {
        // given
        String message = "some random text";

        // when
        messageHandler.handle(message);

        // then
        verify(priceService, never()).process(any());
    }
}