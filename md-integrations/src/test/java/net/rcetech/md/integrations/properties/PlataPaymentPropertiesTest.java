package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlataPaymentPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private PlataPaymentProperties plataPaymentProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("plata-payment-url", plataPaymentProperties.url()),
                () -> assertEquals("plata-payment-token", plataPaymentProperties.token()),
                () -> assertEquals("plata-payment-merchant-id", plataPaymentProperties.merchantId())
        );
    }
}