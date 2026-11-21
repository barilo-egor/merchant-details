package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MeridianPayHighCheckPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private MeridianPayHighCheckProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-high-check-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-high-check-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-high-check-merchant-id", meridianPayProperties.merchantId())
        );
    }
}