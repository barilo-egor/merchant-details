package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtasyPayQrNspkPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ExtasyPayQrNspkProperties extasyPayQrNspkProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("extasy-pay-qr-nspk-url", extasyPayQrNspkProperties.url()),
                () -> assertEquals("extasy-pay-qr-nspk-token", extasyPayQrNspkProperties.token()),
                () -> assertEquals("extasy-pay-qr-nspk-key", extasyPayQrNspkProperties.key()),
                () -> assertEquals("extasy-pay-qr-nspk-secret", extasyPayQrNspkProperties.secret())
        );
    }
}
