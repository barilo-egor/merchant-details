package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class HellBitPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private HellBitProperties hellBitProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("hell-bit-url", hellBitProperties.url()),
                () -> assertEquals("hell-bit-token", hellBitProperties.token()),
                () -> assertEquals("hell-bit-sign-key", hellBitProperties.signKey())
        );
    }
}
