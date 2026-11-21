package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitZonePropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BitZoneProperties bitZoneProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-zone-key", bitZoneProperties.key()),
                () -> assertEquals("bit-zone-url", bitZoneProperties.url())
        );
    }
}