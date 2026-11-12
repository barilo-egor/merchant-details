package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

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