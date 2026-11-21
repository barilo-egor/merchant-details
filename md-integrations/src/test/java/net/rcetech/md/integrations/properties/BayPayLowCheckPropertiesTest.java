package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BayPayLowCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BayPayLowCheckProperties payProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bay-pay-low-check-url", payProperties.url()),
                () -> assertEquals("bay-pay-low-check-api-key", payProperties.apiKey())
        );
    }
}