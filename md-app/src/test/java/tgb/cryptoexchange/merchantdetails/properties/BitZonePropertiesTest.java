package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitZonePropertiesTest extends AbstractMySqlTest {

    @Autowired
    private BitZoneProperties bitZoneProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-zone-key", bitZoneProperties.key()),
                () -> assertEquals("bit-zone-url", bitZoneProperties.url())
        );
    }
}