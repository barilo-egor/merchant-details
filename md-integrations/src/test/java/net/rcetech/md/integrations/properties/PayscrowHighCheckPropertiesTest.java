package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowHighCheckProperties payscrowHighCheckProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-high-check-url", payscrowHighCheckProperties.url()),
                () -> assertEquals("payscrow-high-check-key", payscrowHighCheckProperties.key())
        );
    }
}