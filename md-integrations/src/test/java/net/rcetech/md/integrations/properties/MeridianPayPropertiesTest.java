package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MeridianPayPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private MeridianPayProperties meridianPayProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("meridian-pay-url", meridianPayProperties.url()),
                () -> assertEquals("meridian-pay-token", meridianPayProperties.token()),
                () -> assertEquals("meridian-pay-merchant-id", meridianPayProperties.merchantId())
        );
    }
}