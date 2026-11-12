package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BayPayHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BayPayHighCheckProperties payProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bay-pay-high-check-url", payProperties.url()),
                () -> assertEquals("bay-pay-high-check-api-key", payProperties.apiKey())
        );
    }
}