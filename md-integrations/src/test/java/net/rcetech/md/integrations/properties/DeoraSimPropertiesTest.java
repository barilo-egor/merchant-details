package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DeoraSimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private DeoraSimProperties deoraProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("deora-sim-url", deoraProperties.url()),
                () -> assertEquals("deora-sim-key", deoraProperties.key()),
                () -> assertEquals("deora-sim-secret", deoraProperties.secret())
        );
    }
}