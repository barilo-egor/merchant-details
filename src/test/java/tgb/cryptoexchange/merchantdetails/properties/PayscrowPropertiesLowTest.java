package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesLowTest extends AbstractMySqlTest {

    @Autowired
    private PayscrowLowProperties payscrowLowProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-low-url", payscrowLowProperties.url()),
                () -> assertEquals("payscrow-low-key", payscrowLowProperties.key())
        );
    }
}