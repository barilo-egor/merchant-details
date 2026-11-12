package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SouzPdfPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private SouzPdfProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-pdf-url", souzProperties.url()),
                () -> assertEquals("souz-pdf-key", souzProperties.key()),
                () -> assertEquals("souz-pdf-secret", souzProperties.secret())
        );
    }
}