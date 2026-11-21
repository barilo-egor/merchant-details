package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SouzSimPropertiesTest extends AbstractIntegrationsContextTest {

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