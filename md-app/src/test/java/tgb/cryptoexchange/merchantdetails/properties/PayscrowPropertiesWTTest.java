package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesWTTest extends AbstractMySqlTest {

    @Autowired
    private PayscrowWTProperties payscrowWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-white-triangle-url", payscrowWTProperties.url()),
                () -> assertEquals("payscrow-white-triangle-key", payscrowWTProperties.key())
        );
    }
}