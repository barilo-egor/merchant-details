package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BitBolideDeepLinkPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private BitBolideDeepLinkProperties bitBolideProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("bit-bolide-deep-link-url", bitBolideProperties.url()),
                () -> assertEquals("bit-bolide-deep-link-token", bitBolideProperties.token())
        );
    }
}
