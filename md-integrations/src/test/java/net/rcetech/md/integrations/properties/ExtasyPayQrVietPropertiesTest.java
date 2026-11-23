package net.rcetech.md.integrations.properties;

import net.rcetech.md.integrations.AbstractIntegrationsContextTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExtasyPayQrVietPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private ExtasyPayQrVietProperties extasyPayQrVietProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("extasy-pay-qr-viet-url", extasyPayQrVietProperties.url()),
                () -> assertEquals("extasy-pay-qr-viet-token", extasyPayQrVietProperties.token()),
                () -> assertEquals("extasy-pay-qr-viet-key", extasyPayQrVietProperties.key()),
                () -> assertEquals("extasy-pay-qr-viet-secret", extasyPayQrVietProperties.secret())
        );
    }
}
