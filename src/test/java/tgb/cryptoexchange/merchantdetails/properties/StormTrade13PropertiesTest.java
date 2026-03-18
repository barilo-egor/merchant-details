package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StormTrade13PropertiesTest {

    @Autowired
    private StormTrade13Properties stormTrade13Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("storm-trade13-url", stormTrade13Properties.url()),
                () -> assertEquals("storm-trade13-key", stormTrade13Properties.key()),
                () -> assertEquals("storm-trade13-token", stormTrade13Properties.token()),
                () -> assertEquals("storm-trade13-secret", stormTrade13Properties.secret())
        );
    }
}