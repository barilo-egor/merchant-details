package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StormTrade13PropertiesTest extends AbstractMySqlTest {

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