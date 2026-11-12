package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitBolidePropertiesTest extends AbstractMySqlTest {

    @Autowired
    private BitBolideProperties bitBolideProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-bolide-url", bitBolideProperties.url()),
                () -> assertEquals("bit-bolide-token", bitBolideProperties.token())
        );
    }
}
