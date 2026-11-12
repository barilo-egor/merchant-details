package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PaySyncPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PaySyncProperties paySyncProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("paysync-url", paySyncProperties.url()),
                () -> assertEquals("paysync-client-id", paySyncProperties.clientId())
        );
    }
}