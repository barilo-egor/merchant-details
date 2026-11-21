package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayscrowPropertiesTransgranTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayscrowTransgranProperties payscrowTransgranProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("payscrow-transgran-url", payscrowTransgranProperties.url()),
                () -> assertEquals("payscrow-transgran-key", payscrowTransgranProperties.key())
        );
    }
}