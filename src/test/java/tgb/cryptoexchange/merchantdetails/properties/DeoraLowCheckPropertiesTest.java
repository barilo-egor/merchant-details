package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DeoraLowCheckPropertiesTest {

    @Autowired
    private DeoraLowCheckProperties deoraProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("deora-low-check-url", deoraProperties.url()),
                () -> assertEquals("deora-low-check-key", deoraProperties.key()),
                () -> assertEquals("deora-low-check-secret", deoraProperties.secret())
        );
    }
}