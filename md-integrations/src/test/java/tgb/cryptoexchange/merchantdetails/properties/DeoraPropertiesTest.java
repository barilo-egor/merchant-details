package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeoraPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private DeoraProperties deoraProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("deora-url", deoraProperties.url()),
                () -> assertEquals("deora-key", deoraProperties.key()),
                () -> assertEquals("deora-secret", deoraProperties.secret())
        );
    }
}