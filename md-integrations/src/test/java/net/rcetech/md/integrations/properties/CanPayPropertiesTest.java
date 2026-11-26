package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CanPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private CanPayProperties canPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("can-pay-url", canPayProperties.url()),
                () -> assertEquals("can-pay-api-key", canPayProperties.apiKey())
        );
    }
}
