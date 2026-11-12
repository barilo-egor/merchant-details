package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AsgardImplPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private AsgardImplProperties asgardImplProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("asgard-url", asgardImplProperties.url()),
                () -> assertEquals("asgard-token", asgardImplProperties.token()),
                () -> assertEquals("asgard-secret", asgardImplProperties.secret()),
                () -> assertEquals("asgard-merchant-id", asgardImplProperties.merchantId())
        );
    }
}