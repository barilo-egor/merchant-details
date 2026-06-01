package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class DeoraSimPropertiesTest {

    @Autowired
    private DeoraSimProperties deoraProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("deora-sim-url", deoraProperties.url()),
                () -> assertEquals("deora-sim-key", deoraProperties.key()),
                () -> assertEquals("deora-sim-secret", deoraProperties.secret())
        );
    }
}