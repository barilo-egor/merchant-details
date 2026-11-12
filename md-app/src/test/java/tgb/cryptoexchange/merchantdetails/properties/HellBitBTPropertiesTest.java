package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HellBitBTPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private HellBitBTProperties hellBitBTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("hell-bit-bt-token", hellBitBTProperties.token()),
                () -> assertEquals("hell-bit-bt-sign-key", hellBitBTProperties.signKey())
        );
    }
}
