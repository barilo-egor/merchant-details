package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CrocoPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private CrocoPayImplProperties crocoPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("croco-pay-url", crocoPayProperties.url()),
                () -> assertEquals("croco-pay-client-id", crocoPayProperties.clientId()),
                () -> assertEquals("croco-pay-client-secret", crocoPayProperties.clientSecret())
        );
    }
}