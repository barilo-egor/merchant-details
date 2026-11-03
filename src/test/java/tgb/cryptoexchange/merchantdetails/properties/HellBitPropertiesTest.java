package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class HellBitPropertiesTest {

    @Autowired
    private HellBitProperties hellBitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("hell-bit-url", hellBitProperties.url()),
                () -> assertEquals("hell-bit-token", hellBitProperties.token()),
                () -> assertEquals("hell-bit-sign-key", hellBitProperties.signKey())
        );
    }
}
