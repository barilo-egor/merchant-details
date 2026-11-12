package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitBolideDeepLinkPropertiesTest extends AbstractMySqlTest {

    @Autowired
    private BitBolideDeepLinkProperties bitBolideProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-bolide-deep-link-url", bitBolideProperties.url()),
                () -> assertEquals("bit-bolide-deep-link-token", bitBolideProperties.token())
        );
    }
}
