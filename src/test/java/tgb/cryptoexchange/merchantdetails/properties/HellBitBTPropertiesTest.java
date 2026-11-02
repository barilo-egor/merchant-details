package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HellBitBTPropertiesTest {

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
