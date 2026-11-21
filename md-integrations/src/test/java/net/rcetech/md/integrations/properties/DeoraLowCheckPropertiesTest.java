package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeoraLowCheckPropertiesTest extends AbstractIntegrationsContextTest {

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