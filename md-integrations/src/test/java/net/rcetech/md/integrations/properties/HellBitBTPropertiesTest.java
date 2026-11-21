package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HellBitBTPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private HellBitBTProperties hellBitBTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("hell-bit-bt-token", hellBitBTProperties.token()),
                () -> assertEquals("hell-bit-bt-sign-key", hellBitBTProperties.signKey())
        );
    }
}
