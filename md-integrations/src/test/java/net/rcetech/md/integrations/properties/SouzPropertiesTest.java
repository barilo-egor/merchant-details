package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

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