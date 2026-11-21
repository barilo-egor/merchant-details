package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EvoPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private EvoPayProperties evoPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("evo-pay-url", evoPayProperties.url()),
                () -> assertEquals("evo-pay-key", evoPayProperties.key()),
                () -> assertEquals("evo-pay-change-key", evoPayProperties.changeKey())
        );
    }
}