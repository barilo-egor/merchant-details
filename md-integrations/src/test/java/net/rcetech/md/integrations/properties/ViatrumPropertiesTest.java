package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ViatrumPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ViatrumProperties viatrumProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("viatrum-url", viatrumProperties.url()),
                () -> assertEquals("viatrum-environment", viatrumProperties.environment()),
                () -> assertEquals("viatrum-secret", viatrumProperties.secret()),
                () -> assertEquals("viatrum-pub", viatrumProperties.pub())
        );
    }
}