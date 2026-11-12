package tgb.cryptoexchange.merchantdetails.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tgb.cryptoexchange.merchantdetails.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RostrastPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private RostrastProperties rostrastProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rostrast-url", rostrastProperties.url()),
                () -> assertEquals("rostrast-key", rostrastProperties.key()),
                () -> assertEquals("rostrast-token", rostrastProperties.token()),
                () -> assertEquals("rostrast-secret", rostrastProperties.secret())
        );
    }
}