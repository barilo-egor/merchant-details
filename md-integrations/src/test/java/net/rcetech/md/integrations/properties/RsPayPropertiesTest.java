package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RsPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private RSPayImplProperties rsPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("rs-pay-url", rsPayProperties.url()),
                () -> assertEquals("rs-pay-api-key", rsPayProperties.apiKey()),
                () -> assertEquals("rs-pay-secret", rsPayProperties.secret())
        );
    }
}