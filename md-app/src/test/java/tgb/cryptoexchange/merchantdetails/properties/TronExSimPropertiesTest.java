package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TronExSimPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private TronExSimProperties tronExProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("tron-ex-sim-url", tronExProperties.url()),
                () -> assertEquals("tron-ex-sim-key", tronExProperties.key())
        );
    }
}