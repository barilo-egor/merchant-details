package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DeoraPdfPropertiesTest {

    @Autowired
    private DeoraPdfProperties deoraProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("deora-pdf-url", deoraProperties.url()),
                () -> assertEquals("deora-pdf-key", deoraProperties.key()),
                () -> assertEquals("deora-pdf-secret", deoraProperties.secret())
        );
    }
}