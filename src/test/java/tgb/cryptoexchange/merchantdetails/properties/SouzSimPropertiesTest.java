package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SouzSimPropertiesTest {

    @Autowired
    private SouzSimProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-sim-url", souzProperties.url()),
                () -> assertEquals("souz-sim-key", souzProperties.key()),
                () -> assertEquals("souz-sim-secret", souzProperties.secret())
        );
    }
}