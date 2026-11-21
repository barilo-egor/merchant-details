package net.rcetech.md.integrations.properties;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import net.rcetech.md.integrations.AbstractIntegrationsContextTest;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AsgardWTPropertiesTest extends AbstractIntegrationsContextTest {

    @Autowired
    private AsgardWTProperties asgardWTProperties;

    @Test
    void shouldLoadProperties() {
        assertAll(
                () -> assertEquals("asgard-wt-url", asgardWTProperties.url()),
                () -> assertEquals("asgard-wt-token", asgardWTProperties.token()),
                () -> assertEquals("asgard-wt-secret", asgardWTProperties.secret()),
                () -> assertEquals("asgard-wt-merchant-id", asgardWTProperties.merchantId())
        );
    }
}