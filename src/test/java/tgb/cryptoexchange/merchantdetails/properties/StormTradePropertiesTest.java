package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StormTradePropertiesTest {

    @Autowired
    private StormTradeProperties stormTradeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("storm-trade-url", stormTradeProperties.url()),
                () -> assertEquals("storm-trade-key", stormTradeProperties.key()),
                () -> assertEquals("storm-trade-token", stormTradeProperties.token()),
                () -> assertEquals("storm-trade-secret", stormTradeProperties.secret())
        );
    }
}