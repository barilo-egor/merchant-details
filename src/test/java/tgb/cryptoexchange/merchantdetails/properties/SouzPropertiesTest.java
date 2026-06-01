package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SouzPropertiesTest {

    @Autowired
    private SouzProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-url", souzProperties.url()),
                () -> assertEquals("souz-key", souzProperties.key()),
                () -> assertEquals("souz-secret", souzProperties.secret())
        );
    }
}