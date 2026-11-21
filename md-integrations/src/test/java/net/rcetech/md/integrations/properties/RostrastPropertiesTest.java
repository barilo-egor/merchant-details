package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

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