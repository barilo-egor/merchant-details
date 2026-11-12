package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PayLeeQRPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PayLeeQRProperties payLeeProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("pay-lee-qr-url", payLeeProperties.url()),
                () -> assertEquals("pay-lee-qr-token", payLeeProperties.token()),
                () -> assertEquals("pay-lee-qr-secret", payLeeProperties.secret()),
                () -> assertEquals("pay-lee-qr-client-id-salt", payLeeProperties.clientIdSalt())
        );
    }
}