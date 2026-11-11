package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesImplTest extends AbstractMySqlTest {

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