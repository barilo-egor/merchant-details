package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Base51HighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private Base51HighCheckProperties base51Properties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("base51-high-check-url", base51Properties.url()),
                () -> assertEquals("base51-high-check-client-id", base51Properties.clientId()),
                () -> assertEquals("base51-high-check-client-secret", base51Properties.clientSecret())
        );
    }
}