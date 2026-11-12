package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesImplTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowPropertiesImpl payscrowPropertiesImpl;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-url", payscrowPropertiesImpl.url()),
                () -> assertEquals("payscrow-key", payscrowPropertiesImpl.key())
        );
    }
}