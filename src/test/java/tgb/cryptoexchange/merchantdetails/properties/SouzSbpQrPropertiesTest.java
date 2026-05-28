package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SouzSbpQrPropertiesTest {

    @Autowired
    private SouzSbpQrProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-sbp-qr-url", souzProperties.url()),
                () -> assertEquals("souz-sbp-qr-key", souzProperties.key()),
                () -> assertEquals("souz-sbp-qr-secret", souzProperties.secret())
        );
    }
}