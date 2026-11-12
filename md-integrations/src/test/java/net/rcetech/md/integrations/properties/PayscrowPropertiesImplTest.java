package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesImplTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowPropertiesImpl payscrowPropertiesImpl;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-url", payscrowPropertiesImpl.url()),
                () -> assertEquals("payscrow-key", payscrowPropertiesImpl.key())
        );
    }
}