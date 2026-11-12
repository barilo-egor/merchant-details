package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WatSimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private WatSimProperties watProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wat-sim-token", watProperties.token())
        );
    }
}