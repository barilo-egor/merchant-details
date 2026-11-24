package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BucksPaySimPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BucksPaySimProperties bucksPaySimProperties;

    @DisplayName("Свойства должны загружаться из конфигурации")
    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bucks-pay-sim-url", bucksPaySimProperties.url()),
                () -> assertEquals("bucks-pay-sim-token", bucksPaySimProperties.token()),
                () -> assertEquals("bucks-pay-sim-sign-key", bucksPaySimProperties.signKey())
        );
    }
}