package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CashOutPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private CashOutProperties cashOutProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("cashout-url", cashOutProperties.url()),
                () -> assertEquals("cashout-key", cashOutProperties.key())
        );
    }
}