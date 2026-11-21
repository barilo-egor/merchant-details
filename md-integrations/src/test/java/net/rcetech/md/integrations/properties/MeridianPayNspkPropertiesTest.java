package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MeridianPayNspkPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private MeridianPayNspkProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-nspk-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-nspk-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-nspk-merchant-id", meridianPayProperties.merchantId())
        );
    }
}