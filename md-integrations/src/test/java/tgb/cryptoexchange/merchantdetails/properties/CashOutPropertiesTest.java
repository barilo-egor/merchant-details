package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CashOutPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private CashOutProperties cashOutProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cashout-url", cashOutProperties.url()),
                () -> assertEquals("cashout-key", cashOutProperties.key())
        );
    }
}