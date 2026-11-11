package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractMySqlTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeoraLowCheckPropertiesTest extends AbstractMySqlTest {

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