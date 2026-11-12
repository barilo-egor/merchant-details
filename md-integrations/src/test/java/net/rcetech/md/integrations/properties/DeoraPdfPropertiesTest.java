package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeoraPdfPropertiesTest extends AbstractIntegrationsContextTest {

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