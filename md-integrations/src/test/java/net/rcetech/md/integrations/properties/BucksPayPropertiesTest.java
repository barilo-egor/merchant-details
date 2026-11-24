package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BucksPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BucksPayProperties bucksPayProperties;

    @DisplayName("Свойства должны загружаться из конфигурации")
    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bucks-pay-url", bucksPayProperties.url()),
                () -> assertEquals("bucks-pay-token", bucksPayProperties.token()),
                () -> assertEquals("bucks-pay-sign-key", bucksPayProperties.signKey())
        );
    }
}
