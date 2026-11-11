package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowHighCheckPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private PayscrowHighCheckProperties payscrowHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-high-check-url", payscrowHighCheckProperties.url()),
                () -> assertEquals("payscrow-high-check-key", payscrowHighCheckProperties.key())
        );
    }
}