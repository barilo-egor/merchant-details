package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesWTTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowWTProperties payscrowWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-white-triangle-url", payscrowWTProperties.url()),
                () -> assertEquals("payscrow-white-triangle-key", payscrowWTProperties.key())
        );
    }
}