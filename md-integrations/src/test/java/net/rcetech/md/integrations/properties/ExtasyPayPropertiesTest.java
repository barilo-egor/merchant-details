package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtasyPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ExtasyPayProperties extasyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("extasy-pay-url", extasyPayProperties.url()),
                () -> assertEquals("extasy-pay-token", extasyPayProperties.token()),
                () -> assertEquals("extasy-pay-key", extasyPayProperties.key()),
                () -> assertEquals("extasy-pay-secret", extasyPayProperties.secret())
        );
    }
}
