package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitBolideWTPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private BitBolideWTProperties bitBolideProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-bolide-wt-url", bitBolideProperties.url()),
                () -> assertEquals("bit-bolide-wt-token", bitBolideProperties.token())
        );
    }
}