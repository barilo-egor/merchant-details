package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WatPropertiesImplTest extends AbstractIntegrationsContextTest {

    @Autowired
    private WatPropertiesImpl watProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("wat-url", watProperties.url()),
                () -> assertEquals("wat-token", watProperties.token())
        );
    }
}