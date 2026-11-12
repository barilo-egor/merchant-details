package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ManyPayHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ManyPayHighCheckProperties manyPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("many-pay-high-check-url", manyPayProperties.url()),
                () -> assertEquals("many-pay-high-check-token", manyPayProperties.token())
        );
    }
}