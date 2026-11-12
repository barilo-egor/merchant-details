package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SouzPropertiesTest extends AbstractIntegrationsContextTest {

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