package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class BitBolideDeepLinkPropertiesTest {

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
