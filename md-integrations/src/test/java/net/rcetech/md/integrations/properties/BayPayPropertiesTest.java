package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BayPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BayPayImplProperties payProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bay-pay-url", payProperties.url()),
                () -> assertEquals("bay-pay-api-key", payProperties.apiKey())
        );
    }
}