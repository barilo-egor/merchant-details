package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NorosHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private NorosHighCheckProperties norosHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("noros-high-check-url", norosHighCheckProperties.url()),
                () -> assertEquals("noros-high-check-key", norosHighCheckProperties.key())
        );
    }

}