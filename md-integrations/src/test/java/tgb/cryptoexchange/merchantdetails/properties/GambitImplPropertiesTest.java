package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GambitImplPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private GambitImplProperties gambitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("gambit-url", gambitProperties.url()),
                () -> assertEquals("gambit-key", gambitProperties.key()),
                () -> assertEquals("gambit-terminal", gambitProperties.terminal())
        );
    }
}