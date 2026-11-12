package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GambitSimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private GambitSimProperties gambitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("gambit-sim-url", gambitProperties.url()),
                () -> assertEquals("gambit-sim-key", gambitProperties.key()),
                () -> assertEquals("gambit-sim-terminal", gambitProperties.terminal())
        );
    }
}