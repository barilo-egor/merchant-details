package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SouzSbpQrPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private SouzSbpQrProperties souzProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("souz-sbp-qr-url", souzProperties.url()),
                () -> assertEquals("souz-sbp-qr-key", souzProperties.key()),
                () -> assertEquals("souz-sbp-qr-secret", souzProperties.secret())
        );
    }
}