package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class WellBitPropertiesTest {

    @Autowired
    private WellBitProperties wellBitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("well-bit-url", wellBitProperties.url()),
                () -> assertEquals("well-bit-token", wellBitProperties.token()),
                () -> assertEquals("well-bit-id", wellBitProperties.id()),
                () -> assertEquals("well-bit-login", wellBitProperties.login()),
                () -> assertEquals("well-bit-secret", wellBitProperties.secret())
        );
    }
}