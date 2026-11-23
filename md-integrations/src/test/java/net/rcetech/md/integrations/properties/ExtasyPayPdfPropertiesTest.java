package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtasyPayPdfPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ExtasyPayPdfProperties extasyPayPdfProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("extasy-pay-pdf-url", extasyPayPdfProperties.url()),
                () -> assertEquals("extasy-pay-pdf-token", extasyPayPdfProperties.token()),
                () -> assertEquals("extasy-pay-pdf-key", extasyPayPdfProperties.key()),
                () -> assertEquals("extasy-pay-pdf-secret", extasyPayPdfProperties.secret())
        );
    }
}
