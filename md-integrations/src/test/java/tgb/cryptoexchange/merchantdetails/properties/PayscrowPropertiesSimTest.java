package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesSimTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowSimProperties payscrowSimProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-sim-url", payscrowSimProperties.url()),
                () -> assertEquals("payscrow-sim-key", payscrowSimProperties.key())
        );
    }
}