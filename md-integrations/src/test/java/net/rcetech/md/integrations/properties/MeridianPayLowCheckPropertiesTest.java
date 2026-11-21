package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MeridianPayLowCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private MeridianPayLowCheckProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-low-check-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-low-check-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-low-check-merchant-id", meridianPayProperties.merchantId())
        );
    }
}