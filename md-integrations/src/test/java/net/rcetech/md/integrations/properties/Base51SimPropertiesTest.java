package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Base51SimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private Base51SimProperties base51Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("base51-sim-url", base51Properties.url()),
                () -> assertEquals("base51-sim-client-id", base51Properties.clientId()),
                () -> assertEquals("base51-sim-client-secret", base51Properties.clientSecret())
        );
    }
}