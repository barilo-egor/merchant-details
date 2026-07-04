package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SouzPdfPropertiesTest {

    @Autowired
    private SouzPdfProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-pdf-url", souzProperties.url()),
                () -> assertEquals("souz-pdf-key", souzProperties.key()),
                () -> assertEquals("souz-pdf-secret", souzProperties.secret())
        );
    }
}